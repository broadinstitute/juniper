package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.dao.kit.KitRequestDao;
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
        this.enrolleeService = enrolleeService;
        this.profileService = profileService;
        this.objectMapper = objectMapper;
    }

    public KitRequest requestKit(Enrollee enrollee, String kitType) throws JsonProcessingException {
        // create and save kit request
        Profile profile = profileService.loadWithMailingAddress(enrollee.getProfileId())
                .orElseThrow(() -> new RuntimeException("Missing profile for enrollee: " + enrollee.getShortcode()));
        PepperKitAddress pepperKitAddress = makePepperKitAddress(profile);
        var kitRequest = createKitRequest(enrollee, pepperKitAddress, kitType);

        // TODO: send kit request to DSM

        // TODO: save DSM response with Juniper KitRequest

        return kitRequest;
    }

    private KitRequest createKitRequest(Enrollee enrollee, PepperKitAddress pepperKitAddress, String kitType) throws JsonProcessingException {
        KitRequest kitRequest = KitRequest.builder()
                .enrolleeId(enrollee.getId())
                .kitType(kitType)
                .sentToAddress(makeSentToAddressJson(pepperKitAddress))
                .status(KitRequestStatus.CREATED)
                .build();
        KitRequest savedKitRequest = dao.create(kitRequest);
        logger.info("SampleKit created. id: {}, enrollee: {}", savedKitRequest.getId(), savedKitRequest.getEnrolleeId());
        return savedKitRequest;
    }

    private PepperKitAddress makePepperKitAddress(Profile profile) {
        MailingAddress mailingAddress = profile.getMailingAddress();
        return new PepperKitAddress(
                profile.getGivenName(),
                profile.getFamilyName(),
                mailingAddress.getStreet1(),
                mailingAddress.getStreet2(),
                mailingAddress.getCity(),
                mailingAddress.getState(),
                mailingAddress.getPostalCode(),
                mailingAddress.getCountry(),
                profile.getPhoneNumber()
        );
    }

    private String makeSentToAddressJson(PepperKitAddress pepperKitAddress) throws JsonProcessingException {
        return objectMapper.writeValueAsString(pepperKitAddress);
    }

    private final EnrolleeService enrolleeService;
    private final ProfileService profileService;
    private final ObjectMapper objectMapper;
}
