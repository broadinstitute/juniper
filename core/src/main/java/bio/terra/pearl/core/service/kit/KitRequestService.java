package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.dao.kit.KitRequestDao;
import bio.terra.pearl.core.dao.kit.KitTypeDao;
import bio.terra.pearl.core.dao.study.StudyDao;
import bio.terra.pearl.core.dao.study.StudyEnvironmentDao;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.MailingAddress;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ProfileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class KitRequestService extends CrudService<KitRequest, KitRequestDao> {
    private static final Logger logger = LoggerFactory.getLogger(KitRequestService.class);

    // NOTE: These are completely made up for now, until we get further with the Pepper integration and know what the real
    // possible statuses are.
    private static final Set<String> PEPPER_COMPLETED_STATUSES = Set.of("PROCESSED", "CANCELLED");
    private static final Set<String> PEPPER_FAILED_STATUSES = Set.of("CONTAMINATED");

    public KitRequestService(KitRequestDao dao,
                             @Lazy EnrolleeService enrolleeService,
                             KitTypeDao kitTypeDao,
                             PepperDSMClient pepperDSMClient,
                             ProfileService profileService,
                             StudyDao studyDao,
                             StudyEnvironmentDao studyEnvironmentDao,
                             ObjectMapper objectMapper) {
        super(dao);
        this.enrolleeService = enrolleeService;
        this.kitTypeDao = kitTypeDao;
        this.pepperDSMClient = pepperDSMClient;
        this.profileService = profileService;
        this.studyDao = studyDao;
        this.studyEnvironmentDao = studyEnvironmentDao;
        this.objectMapper = objectMapper;
    }

    /**
     * Send a request for a sample kit to Pepper.
     */
    @Transactional
    public KitRequest requestKit(AdminUser adminUser, Enrollee enrollee, String kitTypeName)
            throws JsonProcessingException {
        // create and save kit request
        Profile profile = profileService.loadWithMailingAddress(enrollee.getProfileId())
                .orElseThrow(() -> new RuntimeException("Missing profile for enrollee: " + enrollee.getShortcode()));
        PepperKitAddress pepperKitAddress = makePepperKitAddress(profile);
        var kitRequest = createKitRequest(adminUser, enrollee, pepperKitAddress, kitTypeName);

        // send kit request to DSM
        var result = pepperDSMClient.sendKitRequest(enrollee, kitRequest, pepperKitAddress);

        // save DSM response/status with Juniper KitRequest
        kitRequest.setDsmStatus(result);
        dao.update(kitRequest);

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
    public Collection<KitRequest> getSampleKitsForStudyEnvironment(StudyEnvironment studyEnvironment) {
        var allKitTypes = kitTypeDao.findAll();
        var kitTypeMap = allKitTypes.stream().collect(Collectors.toMap(KitType::getId, Function.identity()));
        var kits = dao.findByStudyEnvironment(studyEnvironment.getId());
        kits.forEach(kit -> {
            kit.setKitType(kitTypeMap.get(kit.getKitTypeId()));
            try {
                kit.setPepperStatus(objectMapper.readValue(kit.getDsmStatus(), PepperKitStatus.class));
            } catch (JsonProcessingException e) {
                // This is unexpected, so we should log it. However, we will suppress the exception.
                // If we were to propagate it, then unexpected JSON from Pepper would result in
                // not being able to render the kit list page at all.
                logger.warn("Unable to parse JSON from Pepper: {}", kit.getDsmStatus());
            }
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
    public PepperKitStatus syncKitStatusFromPepper(UUID kitId) {
        var kitRequest = dao.find(kitId).orElseThrow(() -> new NotFoundException("Kit request not found"));
        var pepperKitStatus = pepperDSMClient.fetchKitStatus(kitId);
        try {
            kitRequest.setDsmStatus(objectMapper.writeValueAsString(pepperKitStatus));
            kitRequest.setDsmStatusFetchedAt(Instant.now());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not parse JSON response from DSM", e);
        }
        dao.update(kitRequest);
        return pepperKitStatus;
    }

    /**
     * Query Pepper for all in-progress kits and update the cached status in Juniper. This is intended to be called as a
     * scheduled job during expected non-busy times for DSM. If on-demand updates are needed outside the scheduled job,
     * use {@link KitRequestService#updateKitStatus(AdminUser, UUID)} for a single kit or a batch operation that queries
     * Pepper for less than all open kits.
     */
    @Transactional
    public void syncAllKitStatusesFromPepper() {
        var studies = studyDao.findAll();
        for (Study study : studies) {

            // This assumes that DSM is configured with a single study backing all environments of the Juniper study
            var pepperKitStatuses = pepperDSMClient.fetchKitStatusByStudy(study.getId());
            var pepperStatusFetchedAt = Instant.now();
            var pepperKitStatusByKitId = pepperKitStatuses.stream().collect(
                    Collectors.toMap(PepperKitStatus::getKitId, Function.identity()));

            var studyEnvironments = studyEnvironmentDao.findByStudy(study.getId());
            for (StudyEnvironment studyEnvironment : studyEnvironments) {
                var incompleteKits = dao.findIncompleteKits(studyEnvironment.getId());

                // The set of kits returned from DSM may be different from the set of incomplete kits in Juniper, but
                // we want to update the records in Juniper so those are the ones we want to iterate here.
                for (KitRequest kit : incompleteKits) {
                    var pepperKitStatus = pepperKitStatusByKitId.get(kit.getId().toString());
                    if (pepperKitStatus != null) {
                        saveKitStatus(kit, pepperKitStatus, pepperStatusFetchedAt);
                    }
                }
            }
        }
    }

    /**
     * Delete kits for an enrollee. Only for use by populate functions.
     */
    public void deleteByEnrolleeId(UUID enrolleeId, Set<CascadeProperty> cascade) {
        for (KitRequest kitRequest : dao.findByEnrollee(enrolleeId)) {
            dao.delete(kitRequest.getId());
        }
    }

    private KitRequest createKitRequest(
            AdminUser adminUser,
            Enrollee enrollee,
            PepperKitAddress pepperKitAddress,
            String kitTypeName) throws JsonProcessingException {
        KitType kitType = kitTypeDao.findByName(kitTypeName).get();
        KitRequest kitRequest = KitRequest.builder()
                .creatingAdminUserId(adminUser.getId())
                .enrolleeId(enrollee.getId())
                .kitTypeId(kitType.getId())
                .sentToAddress(objectMapper.writeValueAsString(pepperKitAddress))
                .status(KitRequestStatus.CREATED)
                .build();
        KitRequest savedKitRequest = dao.create(kitRequest);
        logger.info("SampleKit created. id: {}, enrollee: {}", savedKitRequest.getId(), savedKitRequest.getEnrolleeId());
        return savedKitRequest;
    }

    /**
     * Saves updated kit status. This is called from a batch job, so exceptions are caught and logged instead of thrown
     * to allow the rest of the batch to be processed.
     */
    private void saveKitStatus(KitRequest kit, PepperKitStatus pepperKitStatus, Instant pepperStatusFetchedAt) {
        try {
            kit.setDsmStatus(objectMapper.writeValueAsString(pepperKitStatus));
            kit.setDsmStatusFetchedAt(pepperStatusFetchedAt);
            kit.setStatus(statusFromDSMStatus(pepperKitStatus.getCurrentStatus()));
            dao.update(kit);
        } catch (JsonProcessingException e) {
            logger.warn(
                    "Unable to serialize status JSON for kit %s: %s".formatted(kit.getId(), pepperKitStatus.toString()),
                    e);
        }
    }

    private KitRequestStatus statusFromDSMStatus(String currentStatus) {
        if (PEPPER_COMPLETED_STATUSES.contains(currentStatus)) {
            return KitRequestStatus.COMPLETE;
        } else if (PEPPER_FAILED_STATUSES.contains(currentStatus)) {
            return KitRequestStatus.FAILED;
        } else {
            return KitRequestStatus.IN_PROGRESS;
        }
    }

    private final EnrolleeService enrolleeService;
    private final KitTypeDao kitTypeDao;
    private final PepperDSMClient pepperDSMClient;
    private final ProfileService profileService;
    private final StudyDao studyDao;
    private final StudyEnvironmentDao studyEnvironmentDao;
    private final ObjectMapper objectMapper;
}
