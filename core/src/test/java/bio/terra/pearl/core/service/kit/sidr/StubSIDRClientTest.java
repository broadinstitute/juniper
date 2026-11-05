package bio.terra.pearl.core.service.kit.sidr;

import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.service.kit.sidr.models.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StubSIDRClientTest {
    private final StubSIDRClient client = new StubSIDRClient();
    private final StudyEnvironmentConfig config = StudyEnvironmentConfig.builder().build();

    private SIDROrder orderWithStreet(String street) {
        return SIDROrder.builder().projectKey("PROJ").externalOrderId("kit-1")
                .samples(List.of(SIDRSample.builder()
                        .shippingInformation(SIDRShippingInformation.builder()
                                .patientAddress(SIDRKitAddress.builder().line1(street).city("Cambridge").build())
                                .build())
                        .patient(SIDRPatient.builder().patientId("HDSALK").build())
                        .build()))
                .build();
    }

    @Test
    void returnsGeneratedIds() {
        SIDROrder result = client.postOrder(config, new AdminUser(), orderWithStreet("1 Main"));
        assertTrue(result.getOrderId().startsWith(StubSIDRClient.STUB_ORDER_PREFIX));
        assertEquals("kit-1", result.getExternalOrderId());
        assertNotNull(result.getSamples().get(0).getSidrSampleId());
        assertEquals("HDSALK", result.getSamples().get(0).getPatient().getPatientId());
    }

    @Test
    void rejectsBadAddresses() {
        SIDRApiException e = assertThrows(SIDRApiException.class,
                () -> client.postOrder(config, new AdminUser(), orderWithStreet("BAD street")));
        assertTrue(e.isAddressValidationError());
        assertEquals("Could not validate the participant's address", e.getMessage());
    }

    @Test
    void acceptsBadAddressesWhenVerificationIsOverridden() {
        SIDROrder order = orderWithStreet("BAD street");
        order.getSamples().get(0).getShippingInformation().getPatientAddress().setVerifyStrict(false);
        assertNotNull(client.postOrder(config, new AdminUser(), order).getOrderId());
    }

    @Test
    void wrapperChoosesClientByConfig() {
        LiveSIDRClient live = new LiveSIDRClient(org.springframework.web.reactive.function.client.WebClient.builder(),
                new com.fasterxml.jackson.databind.ObjectMapper());
        SIDRClientWrapper wrapper = new SIDRClientWrapper(live, client);
        assertSame(client, wrapper.getSIDRClient(StudyEnvironmentConfig.builder().useStubSidr(true).build()));
        assertSame(live, wrapper.getSIDRClient(StudyEnvironmentConfig.builder().useStubSidr(false).build()));
    }
}
