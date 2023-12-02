package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.dao.DaoUtils;
import bio.terra.pearl.core.dao.kit.KitRequestDao;
import bio.terra.pearl.core.dao.kit.KitTypeDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.kit.StudyEnvironmentKitType;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.MailingAddress;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.kit.pepper.*;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ProfileService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyService;
import bio.terra.pearl.core.service.workflow.EventService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class KitRequestService extends CrudService<KitRequest, KitRequestDao> {
    // NOTE: These are completely made up for now, until we get further with the Pepper integration and know what the real
    // possible statuses are.
    private static final Set<String> PEPPER_COMPLETED_STATUSES = Set.of("PROCESSED", "CANCELLED");
    private static final Set<String> PEPPER_FAILED_STATUSES = Set.of("CONTAMINATED");
    private final DaoUtils daoUtils;

    public KitRequestService(KitRequestDao dao,
                             EventService eventService, StudyEnvironmentKitTypeService studyEnvironmentKitTypeService, @Lazy EnrolleeService enrolleeService,
                             KitTypeDao kitTypeDao,
                             PepperDSMClient pepperDSMClient,
                             ProfileService profileService,
                             @Lazy StudyEnvironmentService studyEnvironmentService,
                             @Lazy StudyService studyService,
                             ObjectMapper objectMapper,
                             DaoUtils daoUtils) {
        super(dao);
        this.eventService = eventService;
        this.studyEnvironmentKitTypeService = studyEnvironmentKitTypeService;
        this.enrolleeService = enrolleeService;
        this.kitTypeDao = kitTypeDao;
        this.pepperDSMClient = pepperDSMClient;
        this.profileService = profileService;
        this.studyEnvironmentService = studyEnvironmentService;
        this.studyService = studyService;
        this.objectMapper = objectMapper;
        this.daoUtils = daoUtils;
    }

    /**
     * Send a request for a sample kit to Pepper.
     * Throws PepperApiException if the Pepper API request failed
     */
    public KitRequest requestKit(AdminUser adminUser, String studyShortcode, Enrollee enrollee, String kitTypeName)
    throws PepperApiException {
        // create and save kit request
        if (enrollee.getProfileId() == null) {
            throw new IllegalArgumentException("No profile for enrollee: " + enrollee.getShortcode());
        }
        Profile profile = profileService.loadWithMailingAddress(enrollee.getProfileId()).get();
        PepperKitAddress pepperKitAddress = makePepperKitAddress(profile);
        KitRequest kitRequest = assemble(adminUser, enrollee, pepperKitAddress, kitTypeName);

        // send kit request to DSM
        try {
            PepperKit dsmKitStatus = pepperDSMClient.sendKitRequest(studyShortcode, enrollee, kitRequest, pepperKitAddress);
            // write out the PepperKitStatus as a string for storage
            String pepperRequestJson = objectMapper.writeValueAsString(dsmKitStatus);
            kitRequest.setExternalKit(pepperRequestJson);
        } catch (PepperParseException e) {
            // response was successful, but we got unexpected format back from pepper
            // we want to log the error, but still continue on to saving the kit
            log.error("Unable to parse kit response status from Pepper: kit id %s", kitRequest.getId());
        } catch (JsonProcessingException e) {
            // serialization failures shouldn't ever happen in the objectMapper.writeValueAsString, but don't abort the operation, since the
            // pepper request was already successful
            log.error("Unable to write kit response status from Pepper: kit id %s", kitRequest.getId());
        }
        kitRequest = dao.createWithIdSpecified(kitRequest);
        log.info("Kit request created: enrollee: {}, kit: {}", enrollee.getShortcode(), kitRequest.getId());
        return kitRequest;
    }

    /**
     * Collect the address fields sent to Pepper with a kit request. This is not the full DSM request, just the address
     * information captured at the time of, and stored with, the kit request.
     */
    public static PepperKitAddress makePepperKitAddress(Profile profile) {
        MailingAddress mailingAddress = profile.getMailingAddress();
        return PepperKitAddress.builder()
                .firstName(profile.getGivenName())
                .lastName(profile.getFamilyName())
                .street1(mailingAddress.getStreet1())
                .street2(mailingAddress.getStreet2())
                .city(mailingAddress.getCity())
                .state(mailingAddress.getState())
                .postalCode(mailingAddress.getPostalCode())
                .country(mailingAddress.getCountry())
                .phoneNumber(profile.getPhoneNumber())
                .build();
    }

    /**
     * Fetch all kits for an enrollee.
     */
    public Collection<KitRequest> getKitRequests(Enrollee enrollee) {
        return dao.findByEnrollee(enrollee.getId());
    }

    /**
     * Fetch all sample kits for a study environment.
     */
    public Collection<KitRequest> getSampleKitsByStudyEnvironment(StudyEnvironment studyEnvironment) {
        var allKitTypes = kitTypeDao.findAll();
        var kitTypeMap = allKitTypes.stream().collect(Collectors.toMap(KitType::getId, Function.identity()));
        var kits = dao.findByStudyEnvironment(studyEnvironment.getId());
        kits.forEach(kit -> {
            kit.setKitType(kitTypeMap.get(kit.getKitTypeId()));
            enrolleeService.find(kit.getEnrolleeId()).ifPresent(kit::setEnrollee);
        });
        return kits;
    }

    /**
     * Query Pepper for the status of a single kit and update the cached status in Juniper.
     * This is intended for the special case of needing the absolute most up-to-date information for a single kit.
     * Do _NOT_ call this repeatedly for a collection of kits. Use a bulk operation instead to avoid overwhelming DSM.
     */
    @Transactional
    public PepperKit syncKitStatusFromPepper(UUID kitId) throws PepperParseException, PepperApiException {
        KitRequest kitRequest = dao.find(kitId).orElseThrow(() -> new NotFoundException("Kit request not found"));
        var pepperKitStatus = pepperDSMClient.fetchKitStatus(kitId);
        try {
            kitRequest.setExternalKit(objectMapper.writeValueAsString(pepperKitStatus));
            kitRequest.setExternalKitFetchedAt(Instant.now());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not parse JSON response from DSM", e);
        }
        dao.update(kitRequest);
        return pepperKitStatus;
    }

    /**
     * Query Pepper for all in-progress kits and update the cached status in Juniper. This is intended to be called as a
     * scheduled job during expected non-busy times for DSM. If on-demand updates are needed outside the scheduled job,
     * use {@link KitRequestService#syncKitStatusFromPepper} for a single kit or a batch operation that queries
     * Pepper for less than all open kits.
     */
    @Transactional
    public void syncAllKitStatusesFromPepper() {
        // first get a list of all studies that have kit types configured
        List<StudyEnvironmentKitType> envKitTypes = studyEnvironmentKitTypeService.findAll();
        List<UUID> studyEnvIds = envKitTypes.stream().map(StudyEnvironmentKitType::getStudyEnvironmentId).distinct().toList();
        List<StudyEnvironment> studyEnvs = studyEnvironmentService.findAll(studyEnvIds);
        List<Study> studies = studyService.findAll(
                studyEnvs.stream().map(StudyEnvironment::getStudyId).distinct().collect(Collectors.toList())
        );
        // it doesn't actually matter what order we process the studies in, but it's nice for logging to have them
        // consistently alphabetical
        studies.sort(Comparator.comparing(Study::getShortcode));
        for (Study study : studies) {
            // for each study, grab all the statuses from Pepper
            // (Pepper doesn't have a concept of study environments, so all kits from a study are under the same code)
            // then update the statuses in Juniper for each environment
           try {
               Collection<PepperKit> pepperKits = pepperDSMClient.fetchKitStatusByStudy(study.getShortcode());
               // now find the environments for this study from the list of environments with kit types
               studyEnvs.stream().filter(studyEnv -> studyEnv.getStudyId().equals(study.getId())).forEach( studyEnv -> {
                   syncKitStatusesForStudyEnv(study.getShortcode(), studyEnv.getEnvironmentName(), pepperKits);
               });
            } catch (PepperParseException | PepperApiException e) {
                // if one sync fails, keep trying others in case the failure is just isolated unexpected data
                log.error("kit status sync failed for study %s".formatted(study.getShortcode()), e);
            }
        }
    }

    @Transactional
    public void syncKitStatusesForStudyEnv(Study study, EnvironmentName environmentName) throws PepperParseException, PepperApiException {
        Collection<PepperKit> pepperKits = pepperDSMClient.fetchKitStatusByStudy(study.getShortcode());
        syncKitStatusesForStudyEnv(study.getShortcode(), environmentName, pepperKits);
    }

    private void syncKitStatusesForStudyEnv(String studyShortcode, EnvironmentName envName, Collection<PepperKit> pepperKits) throws PepperParseException, PepperApiException {
        UUID studyEnvId = studyEnvironmentService.findByStudy(studyShortcode, envName)
                .orElseThrow(() -> new NotFoundException("No matching study")).getId();
        var pepperStatusFetchedAt = Instant.now();
        var pepperKitStatusByKitId = pepperKits.stream().collect(
                Collectors.toMap(PepperKit::getJuniperKitId, Function.identity(),
                        (kit1, kit2) -> !kit1.getCurrentStatus().equals("Deactivated") ? kit1 : kit2));

        studyEnvironmentService.find(studyEnvId).ifPresent(
                studyEnvironment -> {
                    var kits = dao.findByStudyEnvironment(studyEnvironment.getId());

                    // The set of kits returned from DSM may be different from the set of incomplete kits in Juniper, but
                    // we want to update the records in Juniper so those are the ones we want to iterate here.
                    for (KitRequest kit : kits) {
                        var pepperKitStatus = pepperKitStatusByKitId.get(kit.getId().toString());
                        if (pepperKitStatus != null) {
                            saveKitStatus(kit, pepperKitStatus, pepperStatusFetchedAt);
                        }
                    }
                }
        );
    }

    public List<KitRequest> findByStudyEnvironment(UUID studyEnvironmentId) {
        return dao.findByStudyEnvironment(studyEnvironmentId);
    }


    /**
     * Delete kits for an enrollee. Only for use by populate functions.
     */
    public void deleteByEnrolleeId(UUID enrolleeId, Set<CascadeProperty> cascade) {
        for (KitRequest kitRequest : dao.findByEnrollee(enrolleeId)) {
            dao.delete(kitRequest.getId());
        }
    }

    /** Just creates the object -- does not communicate with pepper or save to database.  The created
     * object will have an id so that external requests will be sent on it.  */
    public KitRequest assemble(
            AdminUser adminUser,
            Enrollee enrollee,
            PepperKitAddress pepperKitAddress,
            String kitTypeName) {
        KitType kitType = kitTypeDao.findByName(kitTypeName).get();
        KitRequest kitRequest = KitRequest.builder()
                .id(daoUtils.generateUUID())
                .creatingAdminUserId(adminUser.getId())
                .enrolleeId(enrollee.getId())
                .kitTypeId(kitType.getId())
                .sentToAddress(stringifyPepperAddress(pepperKitAddress))
                .status(KitRequestStatus.CREATED)
                .kitType(kitType)
                .build();
        return kitRequest;
    }

    protected String stringifyPepperAddress(PepperKitAddress kitAddress) {
        try {
            return objectMapper.writeValueAsString(kitAddress);
        } catch (JsonProcessingException e) {
            // There's no good reason for PepperKitAddress serialization to fail, so if it does, something very
            // unexpected is happening.  Throw RuntimeException to ensure @Transactional annotations will rollback.
            throw new RuntimeException(e);
        }
    }

    /**
     * Saves updated kit status. This is called from a batch job, so exceptions are caught and logged instead of thrown
     * to allow the rest of the batch to be processed.
     */
    private void saveKitStatus(KitRequest kit, PepperKit pepperKit, Instant pepperStatusFetchedAt) {
        try {
            kit.setExternalKit(objectMapper.writeValueAsString(pepperKit));
            kit.setExternalKitFetchedAt(pepperStatusFetchedAt);
            KitRequestStatus newStatus = PepperKitStatus.mapToKitRequestStatus(pepperKit.getCurrentStatus());
            if (newStatus != kit.getStatus()) {
               eventService.publishKitStatusEvent() //figure out how to load the enrollee and/or portalParticipantUser
            }
            kit.setStatus(newStatus);
            dao.update(kit);
        } catch (JsonProcessingException e) {
            logger.warn(
                    "Unable to serialize status JSON for kit %s: %s".formatted(kit.getId(), pepperKit.toString()),
                    e);
        }
    }


    private final EnrolleeService enrolleeService;
    private final KitTypeDao kitTypeDao;
    private final PepperDSMClient pepperDSMClient;
    private final ProfileService profileService;
    private final StudyService studyService;
    private final StudyEnvironmentService studyEnvironmentService;
    private final ObjectMapper objectMapper;
    private final EventService eventService;
    private StudyEnvironmentKitTypeService studyEnvironmentKitTypeService;
}
