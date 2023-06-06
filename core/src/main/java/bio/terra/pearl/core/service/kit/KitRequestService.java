package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.dao.kit.KitRequestDao;
import bio.terra.pearl.core.dao.kit.KitTypeDao;
import bio.terra.pearl.core.dao.study.StudyDao;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.MailingAddress;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.participant.ProfileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class KitRequestService extends CrudService<KitRequest, KitRequestDao> {
    private static final Logger logger = LoggerFactory.getLogger(KitRequestService.class);

    // NOTE: These are completely made up for now, until we get further with the DSM integration and know what the real
    // possible statuses are.
    private static final Set<String> DSM_COMPLETED_STATUSES = Set.of("PROCESSED", "CANCELLED");
    private static final Set<String> DSM_ERRORED_STATUSES = Set.of("CONTAMINATED");

    public KitRequestService(KitRequestDao dao,
                             KitTypeDao kitTypeDao,
                             PepperDSMClient pepperDSMClient,
                             ProfileService profileService,
                             StudyDao studyDao,
                             ObjectMapper objectMapper) {
        super(dao);
        this.kitTypeDao = kitTypeDao;
        this.pepperDSMClient = pepperDSMClient;
        this.profileService = profileService;
        this.studyDao = studyDao;
        this.objectMapper = objectMapper;
    }

    /**
     * Send a request for a sample kit to DSM.
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
     * Collect the address fields sent to DSM with a kit request. This is not the full DSM request, just the address
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
    public Collection<KitRequest> getKitRequests(AdminUser adminUser, Enrollee enrollee) {
        return dao.findByEnrollee(enrollee.getId());
    }

    /**
     * Query DSM for the status of a single kit and update the cached status in Juniper.
     * This is intended for the special case of needing the absolute most up-to-date information for a single kit.
     * Do _NOT_ call this repeatedly for a collection of kits. Use a bulk operation instead to avoid overwhelming DSM.
     */
    @Transactional
    public PepperDSMKitStatus updateKitStatus(UUID kitId) {
        // auth admin user
        // find kit request
        var kitRequest = dao.find(kitId).orElseThrow(() -> new NotFoundException("Kit request not found"));
        // fetch latest status
        var pepperDSMKitStatus = pepperDSMClient.fetchKitStatus(kitId);
        // save latest status
        try {
            kitRequest.setDsmStatus(objectMapper.writeValueAsString(pepperDSMKitStatus));
            kitRequest.setDsmStatusFetchedAt(Instant.now());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not parse JSON response from DSM", e);
        }
        dao.update(kitRequest);
        return pepperDSMKitStatus;
    }

    /**
     * Query DSM for all in-progress kits and update the cached status in Juniper. This is intended to be called as a
     * scheduled job during expected non-busy times for DSM. If on-demand updates are needed outside the scheduled job,
     * use {@link KitRequestService#updateKitStatus(AdminUser, UUID)} for a single kit or a batch operation that queries
     * DSM for less than all open kits.
     */
    @Transactional
    public void updateAllKitStatuses() {
        var studies = studyDao.findAll();
        for (Study study : studies) {
            // find all in-flight kits
            var incompleteKits = dao.findIncompleteKits(study.getId());

            // fetch statuses from DSM
            if (!incompleteKits.isEmpty()) {
                var dsmKitStatuses = pepperDSMClient.fetchKitStatusByStudy(study.getId());
                var dsmStatusFetchedAt = Instant.now();
                var dsmKitStatusByKitId = dsmKitStatuses.stream().collect(
                        Collectors.toMap(PepperDSMKitStatus::getKitId, Function.identity()));
                // The set of kits returned from DSM may be different from the set of incomplete kits in Juniper, but
                // we want to update the records in Juniper so those are the ones we want to iterate here.
                for (KitRequest kit : incompleteKits) {
                    // update each kit
                    var dsmKitStatus = dsmKitStatusByKitId.get(kit.getId().toString());
                    if (dsmKitStatus != null) {
                        saveKitStatus(kit, dsmKitStatus, dsmStatusFetchedAt);
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
    private void saveKitStatus(KitRequest kit, PepperDSMKitStatus dsmKitStatus, Instant dsmStatusFetchedAt) {
        try {
            kit.setDsmStatus(objectMapper.writeValueAsString(dsmKitStatus));
            kit.setDsmStatusFetchedAt(dsmStatusFetchedAt);
            kit.setStatus(statusFromDSMStatus(dsmKitStatus.getCurrentStatus()));
            dao.update(kit);
        } catch (JsonProcessingException e) {
            logger.warn(
                    "Unable to serialize status JSON for kit %s: %s".formatted(kit.getId(), dsmKitStatus.toString()),
                    e);
        }
    }

    private KitRequestStatus statusFromDSMStatus(String currentStatus) {
        if (DSM_COMPLETED_STATUSES.contains(currentStatus)) {
            return KitRequestStatus.COMPLETE;
        } else if (DSM_ERRORED_STATUSES.contains(currentStatus)) {
            return KitRequestStatus.ERRORED;
        } else {
            return KitRequestStatus.IN_PROGRESS;
        }
    }

    private final KitTypeDao kitTypeDao;
    private final PepperDSMClient pepperDSMClient;
    private final ProfileService profileService;
    private final StudyDao studyDao;
    private final ObjectMapper objectMapper;
}
