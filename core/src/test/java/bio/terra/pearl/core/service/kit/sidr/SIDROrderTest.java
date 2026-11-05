package bio.terra.pearl.core.service.kit.sidr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import bio.terra.pearl.core.service.kit.sidr.models.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import org.junit.jupiter.api.Test;

class SIDROrderTest {
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void serializesToSnakeCaseAndOmitsNulls() throws Exception {
        SIDROrder order = SIDROrder.builder()
                .externalOrderId("EXT-1")
                .projectKey("PROJ")
                .sampleKitRequested(true)
                .tests(List.of(SIDRTest.builder().testCode("T1").samples(List.of("S1")).build()))
                .samples(List.of(SIDRSample.builder()
                        .sampleId("S1")
                        .metadata(List.of(SIDRMetadata.of("collection_date", "2024-05-07")))
                        .shippingInformation(SIDRShippingInformation.builder()
                                .patientAddress(SIDRKitAddress.builder().line1("1 Main").zip("02142").build())
                                .build())
                        .build()))
                .build();
        JsonNode json = mapper.readTree(mapper.writeValueAsString(order));
        assertEquals("EXT-1", json.get("external_order_id").asText());
        assertEquals(true, json.get("sample_kit_requested").asBoolean());
        assertEquals("T1", json.get("tests").get(0).get("test_code").asText());
        JsonNode sample = json.get("samples").get(0);
        assertEquals("2024-05-07", sample.get("metadata").get(0).get("value").get(0).asText());
        assertEquals("1 Main", sample.get("shipping_information").get("patient_address").get("line_1").asText());
        assertFalse(json.has("order_id"));
        assertFalse(json.has("quote"));
    }

    @Test
    void deserializesGpoResponse() throws Exception {
        String json = """
                {"order_id":"SDOR-X","project_id":"P","external_order_id":"EXT-1","unknown_field":1,
                 "samples":[{"sidr_sample_id":"SDSM-X","order_ids":["SDOR-X"],
                   "last_modified":"2024-05-07T12:00:00Z",
                   "metadata":[{"key":"k","value":"single"}],
                   "patient":{"sidr_patient_id":"SDPT-X"}}],
                 "links":{"order":"http://x/api/order/SDOR-X"},
                 "messages":{"warnings":[{"type":"w","loc":["a"],"msg":"m"}]}}""";
        SIDROrder order = mapper.readValue(json, SIDROrder.class);
        assertEquals("SDOR-X", order.getOrderId());
        SIDRSample sample = order.getSamples().get(0);
        assertEquals("SDSM-X", sample.getSidrSampleId());
        assertEquals(List.of("single"), sample.getMetadata().get(0).getValue());
        assertEquals("SDPT-X", sample.getPatient().getSidrPatientId());
        assertEquals("m", order.getMessages().getWarnings().get(0).get("msg"));
    }
}
