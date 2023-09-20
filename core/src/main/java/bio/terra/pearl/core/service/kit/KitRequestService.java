package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.dao.kit.KitRequestDao;
import bio.terra.pearl.core.dao.kit.KitTypeDao;
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
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyService;
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
                             @Lazy StudyEnvironmentService studyEnvironmentService,
                             @Lazy StudyService studyService,
                             ObjectMapper objectMapper) {
        super(dao);
        this.enrolleeService = enrolleeService;
        this.kitTypeDao = kitTypeDao;
        this.pepperDSMClient = pepperDSMClient;
        this.profileService = profileService;
        this.studyEnvironmentService = studyEnvironmentService;
        this.studyService = studyService;
        this.objectMapper = objectMapper;
    }

    /**
     * Send a request for a sample kit to Pepper.
     *
     * 1. Create and save the sample kit request in the Juniper database
     * 2. Send the sample kit request to Pepper
     * 3. Record the status response from Pepper in the Juniper database
     *
     * These steps are all performed in a single transaction. However, each could fail independently. If there's an
     * error from Pepper, then the sample kit request will not be saved in Juniper, and it's safe to assume that there
     * is no new request in Pepper. If there's an error saving the status response from Pepper, then it's possible for
     * there to be a request in Pepper that does not exist in Juniper. We could potentially split that step into its own
     * transaction to help avoid that potential inconsistency.
     */
    @Transactional
    public KitRequest requestKit(AdminUser adminUser, String studyShortcode, Enrollee enrollee, String kitTypeName) {
        // create and save kit request
        Profile profile = profileService.loadWithMailingAddress(enrollee.getProfileId())
                .orElseThrow(() -> new RuntimeException("Missing profile for enrollee: " + enrollee.getShortcode()));
        PepperKitAddress pepperKitAddress = makePepperKitAddress(profile);
        var kitRequest = createKitRequest(adminUser, enrollee, pepperKitAddress, kitTypeName);

        // send kit request to DSM
        var response = pepperDSMClient.sendKitRequest(studyShortcode, enrollee, kitRequest, pepperKitAddress);

        // save DSM response/status with Juniper KitRequest
        try {
            var untypedStatuses = PepperKitStatusResponse.extractUntypedKitStatuses(response, objectMapper);
            // Pepper response from requesting it kit must have one and only one kit status
            var pepperStatusJson = objectMapper.writeValueAsString(untypedStatuses.get(0));
            kitRequest.setDsmStatus(pepperStatusJson);
            saveKitStatus(kitRequest, pepperStatusJson, kitRequest.getCreatedAt());
        } catch (JsonProcessingException e) {
            throw new PepperException("Unable to parse response from Pepper: %s".formatted(response), e);
        }

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
        var studies = studyService.findAll();
        for (Study study : studies) {
            syncKitStatusesForStudy(study);
        }
    }

    @Transactional
    public void syncKitStatusesForStudy(Study study) {
        // This assumes that DSM is configured with a single study backing all environments of the Juniper study
        // TODO: delay deserializing to a PepperKitStatus until after it's been recorded on the kit request
        var pepperKitStatuses = pepperDSMClient.fetchKitStatusByStudy(study.getShortcode());
        var pepperStatusFetchedAt = Instant.now();
        var pepperKitStatusByKitId = pepperKitStatuses.stream().collect(
                Collectors.toMap(PepperKitStatus::getJuniperKitId, Function.identity()));

        var studyEnvironments = studyEnvironmentService.findByStudy(study.getId());
        for (StudyEnvironment studyEnvironment : studyEnvironments) {
            var incompleteKits = findIncompleteKits(studyEnvironment.getId());

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

    public List<KitRequest> findIncompleteKits(UUID studyEnvironmentId) {
        return dao.findByStatus(
                studyEnvironmentId,
                List.of(KitRequestStatus.CREATED, KitRequestStatus.IN_PROGRESS));
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
            String kitTypeName) {
        KitType kitType = kitTypeDao.findByName(kitTypeName).get();
        String addressJson = null;
        try {
            addressJson = objectMapper.writeValueAsString(pepperKitAddress);
        } catch (JsonProcessingException e) {
            // Wrap in a RuntimeException to make it trigger rollback from @Transactional methods. There's no good
            // reason for PepperKitAddress serialization to fail, so we can't assume that anything in the current
            // transaction is valid.
            throw new RuntimeException(e);
        }
        KitRequest kitRequest = KitRequest.builder()
                .creatingAdminUserId(adminUser.getId())
                .enrolleeId(enrollee.getId())
                .kitTypeId(kitType.getId())
                .sentToAddress(addressJson)
                .status(KitRequestStatus.CREATED)
                .build();
        KitRequest savedKitRequest = dao.create(kitRequest);
        savedKitRequest.setKitType(kitType);
        logger.info("SampleKit created. id: {}, enrollee: {}", savedKitRequest.getId(), savedKitRequest.getEnrolleeId());
        return savedKitRequest;
    }

    /**
     * Saves updated kit status. This is called from a batch job, so exceptions are caught and logged instead of thrown
     * to allow the rest of the batch to be processed.
     */
    private void saveKitStatus(KitRequest kit, String statusJson, Instant pepperStatusFetchedAt) {
        // Set raw status JSON
        kit.setDsmStatus(statusJson);
        kit.setDsmStatusFetchedAt(pepperStatusFetchedAt);

        // Set Juniper kit status if we can parse the Pepper status
        try {
            var kitStatus = objectMapper.readValue(statusJson, PepperKitStatus.class);
            kit.setStatus(statusFromDSMStatus(kitStatus.getCurrentStatus()));
        } catch (JsonProcessingException e) {
            logger.warn(
                    "Unable to deserialize status JSON for kit %s: %s".formatted(kit.getId(), statusJson.toString()),
                    e);
        }

        dao.update(kit);
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
        if (currentStatus == null) {
            return KitRequestStatus.CREATED;
        } else if (PEPPER_COMPLETED_STATUSES.contains(currentStatus)) {
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
    private final StudyService studyService;
    private final StudyEnvironmentService studyEnvironmentService;
    private final ObjectMapper objectMapper;
}
