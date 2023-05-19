package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.dao.kit.KitRequestDao;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.MailingAddress;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ProfileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class KitRequestService extends CrudService<KitRequest, KitRequestDao> {

    public KitRequestService(KitRequestDao dao, EnrolleeService enrolleeService, ProfileService profileService, ObjectMapper objectMapper) {
        super(dao);
        this.profileService = profileService;
        this.objectMapper = objectMapper;
    }

    public KitRequest requestKit(AdminUser adminUser, Enrollee enrollee, String kitType) throws JsonProcessingException {
        // create and save kit request
        Profile profile = profileService.loadWithMailingAddress(enrollee.getProfileId())
                .orElseThrow(() -> new RuntimeException("Missing profile for enrollee: " + enrollee.getShortcode()));
        PepperKitAddress pepperKitAddress = makePepperKitAddress(profile);
        var kitRequest = createKitRequest(adminUser, enrollee, pepperKitAddress, kitType);

        // TODO: send kit request to DSM

        // TODO: save DSM response with Juniper KitRequest

        return kitRequest;
    }

    private KitRequest createKitRequest(
            AdminUser adminUser,
            Enrollee enrollee,
            PepperKitAddress pepperKitAddress,
            String kitType) throws JsonProcessingException {
        KitRequest kitRequest = KitRequest.builder()
                .creatingAdminUserId(adminUser.getId())
                .enrolleeId(enrollee.getId())
                .kitType(kitType)
                .sentToAddress(objectMapper.writeValueAsString(pepperKitAddress))
                .status(KitRequestStatus.CREATED)
                .build();
        KitRequest savedKitRequest = dao.create(kitRequest);
        logger.info("SampleKit created. id: {}, enrollee: {}", savedKitRequest.getId(), savedKitRequest.getEnrolleeId());
        return savedKitRequest;
    }

    private PepperKitAddress makePepperKitAddress(Profile profile) {
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
