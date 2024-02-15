package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.dao.DaoUtils;
import bio.terra.pearl.core.dao.kit.KitRequestDao;
import bio.terra.pearl.core.dao.kit.KitTypeDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.address.MailingAddress;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.kit.StudyEnvironmentKitType;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.exception.internal.InternalServerException;
import bio.terra.pearl.core.service.kit.pepper.PepperApiException;
import bio.terra.pearl.core.service.kit.pepper.PepperDSMClient;
import bio.terra.pearl.core.service.kit.pepper.PepperKit;
import bio.terra.pearl.core.service.kit.pepper.PepperKitAddress;
import bio.terra.pearl.core.service.kit.pepper.PepperKitStatus;
import bio.terra.pearl.core.service.kit.pepper.PepperParseException;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class KitRequestService extends CrudService<KitRequest, KitRequestDao> {
    private final DaoUtils daoUtils;

    public KitRequestService(KitRequestDao dao,
                             StudyEnvironmentKitTypeService studyEnvironmentKitTypeService, @Lazy EnrolleeService enrolleeService,
                             EventService eventService,
                             KitTypeDao kitTypeDao,
                             PepperDSMClient pepperDSMClient,
                             ProfileService profileService,
                             PortalParticipantUserService portalParticipantUserService,
                             @Lazy StudyEnvironmentService studyEnvironmentService,
                             @Lazy StudyService studyService,
                             ObjectMapper objectMapper,
                             DaoUtils daoUtils) {
        super(dao);
        this.studyEnvironmentKitTypeService = studyEnvironmentKitTypeService;
        this.enrolleeService = enrolleeService;
        this.eventService = eventService;
        this.kitTypeDao = kitTypeDao;
        this.pepperDSMClient = pepperDSMClient;
        this.profileService = profileService;
        this.portalParticipantUserService = portalParticipantUserService;
        this.studyEnvironmentService = studyEnvironmentService;
        this.studyService = studyService;
        this.objectMapper = objectMapper;
        this.daoUtils = daoUtils;
    }

    /**
     * Send a request for a sample kit to Pepper.
     * Throws PepperApiException if the Pepper API request failed
     */
    public KitRequestDto requestKit(AdminUser adminUser, String studyShortcode, Enrollee enrollee, String kitTypeName)
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
            PepperKit pepperKit = pepperDSMClient.sendKitRequest(studyShortcode, enrollee, kitRequest, pepperKitAddress);
            // write out the PepperKitStatus as a string for storage
            String pepperRequestJson = objectMapper.writeValueAsString(pepperKit);
            kitRequest.setExternalKit(pepperRequestJson);
        } catch (PepperParseException e) {
            // response was successful, but we got unexpected format back from pepper
            // we want to log the error, but still continue on to saving the kit
            log.error("Unable to parse kit response status from Pepper: kit id {}", kitRequest.getId());
        } catch (JsonProcessingException e) {
            // serialization failures shouldn't ever happen in the objectMapper.writeValueAsString, but don't abort the operation, since the
            // pepper request was already successful
            log.error("Unable to serialize kit response status from Pepper: kit id {}", kitRequest.getId());
        }
        kitRequest = dao.createWithIdSpecified(kitRequest);
        log.info("Kit request created: enrollee: {}, kit: {}", enrollee.getShortcode(), kitRequest.getId());
        return new KitRequestDto(kitRequest, kitRequest.getKitType(), enrollee.getShortcode(), objectMapper);
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
    public List<KitRequestDto> findByEnrollee(Enrollee enrollee) {
        List<KitRequest> kits = dao.findByEnrollee(enrollee.getId());
        return createKitRequestDto(kits, getKitTypeMap(), getEnrollees(kits));
    }

    /**
     * Fetch all kits for a collection of enrollees
     */
    public Map<UUID, List<KitRequestDto>> findByEnrollees(Collection<Enrollee> enrollees) {
        Map<UUID, Enrollee> idToEnrollee =
            enrollees.stream().collect(Collectors.toMap(Enrollee::getId, Function.identity()));
        Map<UUID, List<KitRequest>> idToKitRequest = dao.findByEnrolleeIds(idToEnrollee.keySet());

        Map<UUID, KitType> kitTypeMap = getKitTypeMap();
        return idToKitRequest.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> createKitRequestDto(entry.getValue(), kitTypeMap, idToEnrollee)
            ));
    }

    /**
     * Fetch all kits for a study environment.
     */
    public Collection<KitRequestDto> getKitsByStudyEnvironment(StudyEnvironment studyEnvironment) {
        List<KitRequest> kits = dao.findByStudyEnvironment(studyEnvironment.getId());
        return createKitRequestDto(kits, getKitTypeMap(), getEnrollees(kits));
    }

    protected List<KitRequestDto> createKitRequestDto(List<KitRequest> kitRequests,
                                                          Map<UUID, KitType> kitTypeMap,
                                                          Map<UUID, Enrollee> enrollees
    ) {
        List<KitRequestDto> kitRequestDto = new ArrayList<>();
        kitRequests.forEach(kit -> {
            String enrolleeShortcode = enrollees.get(kit.getEnrolleeId()).getShortcode();
            KitRequestDto requestDetails =
                new KitRequestDto(kit, kitTypeMap.get(kit.getKitTypeId()), enrolleeShortcode, objectMapper);
            kitRequestDto.add(requestDetails);
        });
        return kitRequestDto;
    }

    protected  Map<UUID, KitType> getKitTypeMap() {
        return kitTypeDao.findAll().stream()
            .collect(Collectors.toMap(KitType::getId, Function.identity()));
    }

    /**
     * Query Pepper for the status of a single kit and update the cached status in Juniper.
     * This is intended for the special case of needing the absolute most up-to-date information for a single kit.
     * Do _NOT_ call this repeatedly for a collection of kits. Use a bulk operation instead to avoid overwhelming DSM.
     */
    @Transactional
    public void syncKitStatusFromPepper(UUID kitId) throws PepperParseException, PepperApiException {
        KitRequest kitRequest = dao.find(kitId).orElseThrow(() -> new NotFoundException("Kit request not found"));
        PepperKit pepperKitStatus = pepperDSMClient.fetchKitStatus(kitId);
        saveKitStatus(kitRequest, pepperKitStatus, Instant.now());
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
                studyEnvs.stream().map(StudyEnvironment::getStudyId).distinct().toList()
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
               studyEnvs.stream().filter(studyEnv -> studyEnv.getStudyId().equals(study.getId())).forEach(studyEnv ->
                   syncKitStatusesForStudyEnv(study.getShortcode(), studyEnv.getEnvironmentName(), pepperKits)
               );
            } catch (PepperParseException | PepperApiException e) {
                // if one sync fails, keep trying others in case the failure is just isolated unexpected data
                log.error("kit status sync failed for study %s".formatted(study.getShortcode()), e);
            }
        }
    }

    @Transactional
    public void syncKitStatusesForStudyEnv(Study study, EnvironmentName environmentName)
            throws PepperParseException, PepperApiException {
        Collection<PepperKit> pepperKits = pepperDSMClient.fetchKitStatusByStudy(study.getShortcode());
        syncKitStatusesForStudyEnv(study.getShortcode(), environmentName, pepperKits);
    }

    private void syncKitStatusesForStudyEnv(String studyShortcode, EnvironmentName envName,
                                            Collection<PepperKit> pepperKits)
            throws PepperParseException, PepperApiException {
        UUID studyEnvId = studyEnvironmentService.findByStudy(studyShortcode, envName)
                .orElseThrow(() -> new NotFoundException("No matching study")).getId();
        Instant pepperStatusFetchedAt = Instant.now();
        Map<String, PepperKit> pepperKitByKitId = pepperKits.stream().collect(
                Collectors.toMap(PepperKit::getJuniperKitId, Function.identity(),
                        (kit1, kit2) -> !kit1.getCurrentStatus().equals("Deactivated") ? kit1 : kit2));

        studyEnvironmentService.find(studyEnvId).ifPresent(studyEnvironment -> {
            List<KitRequest> kits = dao.findByStudyEnvironment(studyEnvironment.getId());

            // The set of kits returned from DSM may be different from the set of incomplete kits in Juniper, but
            // we want to update the records in Juniper so those are the ones we want to iterate here.
            for (KitRequest kitRequest : kits) {
                PepperKit pepperKit = pepperKitByKitId.get(kitRequest.getId().toString());
                if (pepperKit != null) {
                    try {
                        saveKitStatus(kitRequest, pepperKit, pepperStatusFetchedAt);
                    } catch (Exception e) {
                        // continue processing other requests
                        log.error("Error processing kit status update for kit request %s"
                                .formatted(kitRequest.getId()), e);
                    }
                }
            }
        });
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
            throw new InternalServerException("Error serializing PepperKitAddress", e);
        }
    }

    /**
     * Saves updated kit status and creates an event for certain status changes
     */
    private void saveKitStatus(KitRequest kitRequest, PepperKit pepperKit, Instant pepperStatusFetchedAt) {
        KitRequestStatus priorStatus = kitRequest.getStatus();
        try {
            kitRequest.setExternalKit(objectMapper.writeValueAsString(pepperKit));
            kitRequest.setExternalKitFetchedAt(pepperStatusFetchedAt);
            KitRequestStatus status = PepperKitStatus.mapToKitRequestStatus(pepperKit.getCurrentStatus());
            kitRequest.setStatus(status);
            setKitDates(kitRequest, pepperKit);
            // for now just copy these over on each update, since there is currently no reason to make it conditional
            kitRequest.setTrackingNumber(pepperKit.getTrackingNumber());
            kitRequest.setReturnTrackingNumber(pepperKit.getReturnTrackingNumber());
            kitRequest.setErrorMessage(pepperKit.getErrorMessage());
            dao.update(kitRequest);
        } catch (JsonProcessingException e) {
            throw new InternalServerException("Error serializing PepperKit for kit request %s: %s"
                    .formatted(kitRequest.getId(), pepperKit.toString()), e);
        }

        notifyKitStatusChange(kitRequest, priorStatus);
    }

    protected void setKitDates(KitRequest kitRequest, PepperKit pepperKit) {
        try {
            // collect this information regardless of status
            kitRequest.setLabeledAt(parsePepperDateTime(pepperKit.getLabelDate()));
            kitRequest.setSentAt(parsePepperDateTime(pepperKit.getScanDate()));
            kitRequest.setReceivedAt(parsePepperDateTime(pepperKit.getReceiveDate()));
        } catch (Exception e) {
            // continue processing this request even if the date parsing fails
            log.error("Error parsing PepperKit date for kit %s: %s"
                    .formatted(kitRequest.getId(), pepperKit.toString()), e);
        }
    }

    protected void notifyKitStatusChange(KitRequest kitRequest, KitRequestStatus priorStatus) {
        // only notify on status change
        if (priorStatus == kitRequest.getStatus()) {
            return;
        }
        // only notify when the kit is sent or received
        if (!List.of(KitRequestStatus.SENT, KitRequestStatus.RECEIVED).contains(kitRequest.getStatus())) {
            return;
        }

        Enrollee enrollee = getEnrollee(kitRequest);
        PortalParticipantUser ppUser = portalParticipantUserService.findForEnrollee(enrollee);

        eventService.publishKitStatusEvent(kitRequest, enrollee, ppUser, priorStatus);
    }

    protected Enrollee getEnrollee(KitRequest kitRequest) {
        return enrolleeService.find(kitRequest.getEnrolleeId()).orElseThrow(() ->
                new IllegalStateException("Invalid enrollee for KitRequest %s: enrollee ID=%s"
                        .formatted(kitRequest.getId(), kitRequest.getEnrolleeId())));
    }

    protected Map<UUID, Enrollee> getEnrollees(List<KitRequest> kitRequests) {
        List<UUID> enrolleeIds = kitRequests.stream().map(KitRequest::getEnrolleeId).toList();
        return enrolleeService.findAll(enrolleeIds).stream()
            .collect(Collectors.toMap(Enrollee::getId, Function.identity()));
    }

    /**
     * Parse a Pepper date-time string into an Instant. Returns null if the date string is null or empty.
     * @throws java.time.format.DateTimeParseException if the string is not a valid date-time
     */
    protected Instant parsePepperDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isEmpty()) {
            return null;
        }
        return Instant.parse(dateTimeString);
    }

    private final EnrolleeService enrolleeService;
    private final KitTypeDao kitTypeDao;
    private final PepperDSMClient pepperDSMClient;
    private final ProfileService profileService;
    private final StudyService studyService;
    private final StudyEnvironmentService studyEnvironmentService;
    private final ObjectMapper objectMapper;
    private final EventService eventService;
    private final StudyEnvironmentKitTypeService studyEnvironmentKitTypeService;
    private final PortalParticipantUserService portalParticipantUserService;
}
