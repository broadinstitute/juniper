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
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.participant.ProfileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class KitRequestService extends CrudService<KitRequest, KitRequestDao> {
    private final PepperDSMClient pepperDSMClient;
    private final KitTypeDao kitTypeDao;

    public KitRequestService(KitRequestDao dao,
                             PepperDSMClient pepperDSMClient,
                             KitTypeDao kitTypeDao,
                             ProfileService profileService,
                             ObjectMapper objectMapper) {
        super(dao);
        this.pepperDSMClient = pepperDSMClient;
        this.kitTypeDao = kitTypeDao;
        this.profileService = profileService;
        this.objectMapper = objectMapper;
    }

    public KitRequest requestKit(AdminUser adminUser, Enrollee enrollee, String kitTypeName) throws JsonProcessingException {
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

    public Collection<KitRequest> getKitRequests(AdminUser adminUser, Enrollee enrollee) {
        return dao.findByEnrollee(enrollee.getId());
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

    private final ProfileService profileService;
    private final ObjectMapper objectMapper;
}
