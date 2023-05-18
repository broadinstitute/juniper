package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.service.participant.ProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class KitRequestServiceTest extends BaseSpringBootTest {

    @Autowired
    private KitRequestService kitRequestService;

    @Transactional
    @Test
    public void testRequestKit() throws Exception {
        var enrolleeBundle = enrolleeFactory.buildWithPortalUser("testRequestKit");
        var enrollee = enrolleeBundle.enrollee();
        var profile = enrollee.getProfile();
        profile.setGivenName("Alex");
        profile.setFamilyName("Tester");
        profile.setPhoneNumber("111-222-3333");
        profileService.update(profile);
        var expectedSentToAddress = "{\"firstName\":\"Alex\",\"lastName\":\"Tester\",\"street1\":null,\"street2\":null,\"city\":null,\"state\":null,\"postalCode\":null,\"country\":null,\"phoneNumber\":\"111-222-3333\"}";

        var sampleKit = kitRequestService.requestKit(enrollee, "blood");

        assertThat(sampleKit.getEnrolleeId(), equalTo(enrollee.getId()));
        assertThat(sampleKit.getSentToAddress(), equalTo(expectedSentToAddress));
        assertThat(sampleKit.getStatus(), equalTo(KitRequestStatus.CREATED));
    }

    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private ProfileService profileService;

}
