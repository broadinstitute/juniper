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

    /**
     * the order response is stored with the kit by serializing the parsed SIDROrder, so anything SIDR returns
     * that isn't on our models is lost. This is a response in the shape of GPO's order creation output.
     */
    @Test
    void responseFieldsSurviveRoundTrip() throws Exception {
        String response = """
                {"order_id":"SDOR-X","project_id":"PROJ","external_order_id":"kit-1","status":"Order Submitted",
                 "status_history":[{"status_key":"submitted","status_name":"Order Submitted","timestamp":"2024-05-07T12:00:00"}],
                 "last_modified":"2024-05-07T12:00:00",
                 "tests":[{"test_code":"R_DTP_KIT_REC_AND_STORE","version":"1","product":"P-ESH-0106",
                   "test_samples":[{"sample_id":"","sidr_sample_id":"SDSM-X","test_sample_status":"Kit Requested",
                     "client_barcode":"","last_modified":"2024-05-07T12:00:00",
                     "status_history":[{"status_key":"kit_requested","status_name":"Kit Requested","timestamp":"2024-05-07T12:00:00Z"}]}]}],
                 "samples":[{"sidr_sample_id":"SDSM-X","last_modified":"2024-05-07T12:00:00",
                   "shipping_information":{"kit_type":"SALIVA","kit_status":"Requested"},
                   "patient":{"sidr_patient_id":"SDPT-X","last_modified":"2024-05-07T12:00:00"}}],
                 "links":{"order":"http://x/api/order/SDOR-X"}}""";
        SIDROrder order = mapper.readValue(response, SIDROrder.class);
        JsonNode stored = mapper.readTree(mapper.writeValueAsString(order));
        assertEquals("PROJ", stored.get("project_id").asText());
        assertEquals("Order Submitted", stored.get("status").asText());
        assertEquals("submitted", stored.get("status_history").get(0).get("status_key").asText());
        JsonNode test = stored.get("tests").get(0);
        assertEquals("P-ESH-0106", test.get("product").asText());
        assertEquals("Kit Requested", test.get("test_samples").get(0).get("test_sample_status").asText());
        assertEquals("SDSM-X", test.get("test_samples").get(0).get("sidr_sample_id").asText());
        assertEquals("Requested", stored.get("samples").get(0).get("shipping_information").get("kit_status").asText());
        // timestamps are kept as sent: naive and offset forms both parse
        assertEquals("2024-05-07T12:00:00", stored.get("last_modified").asText());
        assertEquals("2024-05-07T12:00:00Z", test.get("test_samples").get(0).get("status_history").get(0).get("timestamp").asText());
    }
}
