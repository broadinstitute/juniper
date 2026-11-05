package bio.terra.pearl.core.service.kit.sidr;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class SIDRApiExceptionTest {
    // captured from SIDR's own tests for an address EasyPost can't verify
    private static final String ADDRESS_INVALID_BODY = """
            {"errors":[{"type":"invalid.address_invalid",
              "loc":["body","order","samples",0,"shipping_information","patient_address"],
              "msg":"Address is invalid: Unable to verify address.",
              "ctx":{"header_error":"Unable to verify address."}}],"code":422}""";

    @Test
    void addressValidationFailuresAreIdentified() {
        SIDRApiException e = SIDRApiException.fromResponse(HttpStatus.UNPROCESSABLE_ENTITY, ADDRESS_INVALID_BODY);
        assertTrue(e.isAddressValidationError());
        assertEquals("Could not validate the participant's address", e.getMessage());
        assertEquals(ADDRESS_INVALID_BODY, e.getResponseBody());
    }

    @Test
    void otherErrorsAreNotAddressFailures() {
        for (String body : new String[]{"{\"errors\":[{\"type\":\"invalid.invalid\",\"msg\":\"nope\"}]}",
                "not json", "", null, "{\"detail\":\"bad\"}"}) {
            SIDRApiException e = SIDRApiException.fromResponse(HttpStatus.UNPROCESSABLE_ENTITY, body);
            assertFalse(e.isAddressValidationError(), "body: " + body);
            assertTrue(e.getMessage().startsWith("Could not place kit order (HTTP 422)"), "body: " + body);
        }
    }

    @Test
    void messageNeverIncludesTheResponseBody() {
        String body = "{\"trace\": \"SECRET-INTERNAL-DETAIL\"}";
        for (HttpStatus status : new HttpStatus[]{HttpStatus.BAD_REQUEST, HttpStatus.FORBIDDEN,
                HttpStatus.UNPROCESSABLE_ENTITY, HttpStatus.INTERNAL_SERVER_ERROR}) {
            SIDRApiException e = SIDRApiException.fromResponse(status, body);
            assertFalse(e.getMessage().contains("SECRET"));
            assertTrue(e.getMessage().contains("HTTP " + status.value()));
            // but it's kept, in full, for logging
            assertEquals(body, e.getResponseBody());
        }
        assertFalse(SIDRApiException.fromResponse(HttpStatus.UNPROCESSABLE_ENTITY,
                ADDRESS_INVALID_BODY + "SECRET").getMessage().contains("SECRET"));
        String longBody = "x".repeat(5000);
        assertEquals(longBody, SIDRApiException.fromResponse(HttpStatus.BAD_GATEWAY, longBody).getResponseBody());
    }
}
