package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.kit.KitTypeFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.service.participant.ProfileService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class KitRequestServiceTest extends BaseSpringBootTest {

    @Transactional
    @Test
    public void testRequestKit() throws Exception {
        var adminUser = adminUserFactory.buildPersisted("testRequestKit");
        var kitType = kitTypeFactory.buildPersisted("testRequestKit");
        var enrolleeBundle = enrolleeFactory.buildWithPortalUser("testRequestKit");
        var enrollee = enrolleeBundle.enrollee();
        var profile = enrollee.getProfile();
        profile.setGivenName("Alex");
        profile.setFamilyName("Tester");
        profile.setPhoneNumber("111-222-3333");
        profileService.update(profile);
        var expectedSentToAddress = "{\"firstName\":\"Alex\",\"lastName\":\"Tester\",\"street1\":null,\"street2\":null,\"city\":null,\"state\":null,\"postalCode\":null,\"country\":null,\"phoneNumber\":\"111-222-3333\"}";

        var sampleKit = kitRequestService.requestKit(adminUser, enrollee, "testRequestKit");

        Mockito.verify(mockPepperDSMClient)
                .sendKitRequest(eq(enrollee), any(KitRequest.class), any(PepperKitAddress.class));
        assertThat(sampleKit.getCreatingAdminUserId(), equalTo(adminUser.getId()));
        assertThat(sampleKit.getEnrolleeId(), equalTo(enrollee.getId()));
        assertThat(sampleKit.getSentToAddress(), equalTo(expectedSentToAddress));
        assertThat(sampleKit.getStatus(), equalTo(KitRequestStatus.CREATED));
    }

    @MockBean
    private PepperDSMClient mockPepperDSMClient;

    @Autowired
    private AdminUserFactory adminUserFactory;
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private KitRequestService kitRequestService;
    @Autowired
    private KitTypeFactory kitTypeFactory;
    @Autowired
    private ProfileService profileService;

}
