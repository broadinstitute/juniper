package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.IntegrationTest;
import bio.terra.pearl.core.factory.kit.KitRequestFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class LivePepperDSMClientIntegrationTest extends BaseSpringBootTest {

    @Autowired
    private LivePepperDSMClient livePepperDSMClient;

    @Transactional
    @IntegrationTest
    public void testSendKitRequestParsesInvalidAddressError() throws Exception {
        // Arrange
        var enrollee = enrolleeFactory.buildPersisted("testSendKitRequestParsesInvalidAddressError");
        var kitRequest = kitRequestFactory.buildPersisted("testSendKitRequestParsesInvalidAddressError", enrollee.getId());
        var address = PepperKitAddress.builder()
                .firstName("Juniper")
                .lastName("Testerson")
                .street1("123 Fake Street")
                .city("Cambridge")
                .state("MA")
                .postalCode("02142")
                .country("USA")
                .build();

        PepperException pepperException = Assertions.assertThrows(PepperException.class, () -> {
            livePepperDSMClient.sendKitRequest(enrollee, kitRequest, address);
        });

        assertThat(pepperException.getErrorResponse().getErrorMessage(), equalTo("UNKNOWN_KIT_TYPE"));
    }

    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private KitRequestFactory kitRequestFactory;
}
