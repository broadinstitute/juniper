package bio.terra.pearl.core.service.kit.sidr;

import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.service.kit.sidr.models.SIDREnvironment;
import bio.terra.pearl.core.service.kit.sidr.models.SIDROrder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** plain unit test: no Spring context, with the SIDR host and Google auth stubbed out */
class LiveSIDRClientTest {
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final StudyEnvironmentConfig config = StudyEnvironmentConfig.builder()
            .useStubSidr(false).sidrEnvironment(SIDREnvironment.STAGING).build();
    private MockWebServer server;
    private LiveSIDRClient client;
    private String requestedAudience;

    @BeforeEach
    void setup() throws Exception {
        server = new MockWebServer();
        server.start();
        client = new LiveSIDRClient(WebClient.builder(), objectMapper) {
            @Override
            protected String buildUri(SIDREnvironment env, String path) {
                return server.url("/" + path).toString();
            }

            @Override
            protected String fetchIdToken(String audience) {
                requestedAudience = audience;
                return "fake-id-token";
            }
        };
    }

    @AfterEach
    void teardown() throws Exception {
        server.shutdown();
    }

    @Test
    void postOrderSendsAuthAndActorAndParsesResponse() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(201).setHeader("Content-Type", "application/json")
                .setBody("{\"order_id\":\"SDOR-1\",\"unknown\":true,\"samples\":[{\"sidr_sample_id\":\"SDSM-1\"}]}"));
        SIDROrder order = SIDROrder.builder().projectKey("PROJ").clinicalOrderAttestation(true)
                .sampleKitRequested(true).externalOrderId("kit-1").build();

        SIDROrder created = client.postOrder(config, AdminUser.builder().username("staff@example.com").build(), order);

        assertEquals("SDOR-1", created.getOrderId());
        assertEquals("SDSM-1", created.getSamples().get(0).getSidrSampleId());
        assertEquals("broad-gpo-staging", requestedAudience);
        RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);
        assertEquals("POST", request.getMethod());
        assertEquals("/api/order", request.getPath());
        assertEquals("Bearer fake-id-token", request.getHeader("Authorization"));
        assertEquals("staff@example.com", request.getHeader("Actor-Email"));
        JsonNode body = objectMapper.readTree(request.getBody().readUtf8());
        assertEquals("PROJ", body.get("project_key").asText());
        assertTrue(body.get("clinical_order_attestation").asBoolean());
        assertFalse(body.has("order_id"));
    }

    @Test
    void postOrderRequiresAnActor() {
        assertThrows(IllegalArgumentException.class, () -> client.postOrder(config, new AdminUser(), new SIDROrder()));
        assertEquals(0, server.getRequestCount());
    }

    @Test
    void errorResponsesBecomeApiExceptions() {
        server.enqueue(new MockResponse().setResponseCode(422).setBody("{\"detail\":\"bad address\"}"));
        SIDRApiException e = assertThrows(SIDRApiException.class, () ->
                client.postOrder(config, AdminUser.builder().username("staff@example.com").build(), new SIDROrder()));
        assertEquals(422, e.getHttpStatusCode().value());
        assertTrue(e.getResponseBody().contains("bad address"));
        assertFalse(e.getMessage().contains("bad address"), "response content isn't in the message");
        assertFalse(e.isAddressValidationError());
    }

    @Test
    void addressValidationFailuresAreIdentified() {
        server.enqueue(new MockResponse().setResponseCode(422).setBody(
                "{\"errors\":[{\"type\":\"invalid.address_invalid\",\"msg\":\"Address is invalid\"}],\"code\":422}"));
        SIDRApiException e = assertThrows(SIDRApiException.class, () ->
                client.postOrder(config, AdminUser.builder().username("staff@example.com").build(), new SIDROrder()));
        assertTrue(e.isAddressValidationError());
        assertEquals("Could not validate the participant's address", e.getMessage());
        assertEquals(422, e.getHttpStatusCode().value());
    }

    @Test
    void unparseableResponseBecomesApiException() {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("not json"));
        assertThrows(SIDRApiException.class, () -> client.getOrder(config, "SDOR-1"));
    }

    @Test
    void getOrderRequestsOrderById() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json")
                .setBody("{\"order_id\":\"SDOR-9\"}"));
        assertEquals("SDOR-9", client.getOrder(config, "SDOR-9").getOrderId());
        RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);
        assertEquals("GET", request.getMethod());
        assertEquals("/api/order/SDOR-9", request.getPath());
        assertEquals("Bearer fake-id-token", request.getHeader("Authorization"));
    }
}
