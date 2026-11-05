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
        assertTrue(e.getMessage().startsWith("ADDRESS_VALIDATION_ERROR"));
        assertTrue(e.getUserMessage().startsWith("ADDRESS_VALIDATION_ERROR"));
        assertEquals(ADDRESS_INVALID_BODY, e.getResponseBody());
    }

    @Test
    void otherErrorsAreNotAddressFailures() {
        for (String body : new String[]{"{\"errors\":[{\"type\":\"invalid.invalid\",\"msg\":\"nope\"}]}",
                "not json", "", null, "{\"detail\":\"bad\"}"}) {
            SIDRApiException e = SIDRApiException.fromResponse(HttpStatus.UNPROCESSABLE_ENTITY, body);
            assertFalse(e.isAddressValidationError(), "body: " + body);
            assertTrue(e.getMessage().startsWith("SIDR returned 422"), "body: " + body);
        }
    }

    @Test
    void messageHasTheBodyForLogsButUserMessageNeverDoes() {
        String body = "{\"trace\": \"SECRET-INTERNAL-DETAIL\"}";
        for (HttpStatus status : new HttpStatus[]{HttpStatus.BAD_REQUEST, HttpStatus.FORBIDDEN,
                HttpStatus.UNPROCESSABLE_ENTITY, HttpStatus.INTERNAL_SERVER_ERROR}) {
            SIDRApiException e = SIDRApiException.fromResponse(status, body);
            assertTrue(e.getMessage().contains("SECRET"));
            assertFalse(e.getUserMessage().contains("SECRET"));
            assertTrue(e.getUserMessage().contains("HTTP " + status.value()));
        }
        // address failures don't pass the body on to users either
        assertFalse(SIDRApiException.fromResponse(HttpStatus.UNPROCESSABLE_ENTITY,
                ADDRESS_INVALID_BODY + "SECRET").getUserMessage().contains("SECRET"));
        // failures before a response (authentication, parsing) don't expose the cause either
        assertFalse(new SIDRApiException("Unable to authenticate to SIDR: key file /secrets/sa.json",
                new RuntimeException()).getUserMessage().contains("secrets"));
    }

    @Test
    void messageIncludesTheWholeBody() {
        String body = "x".repeat(5000);
        SIDRApiException e = SIDRApiException.fromResponse(HttpStatus.BAD_GATEWAY, body);
        assertTrue(e.getMessage().endsWith(body));
        assertEquals(body, e.getResponseBody());
        assertFalse(e.getUserMessage().contains("xxx"));
    }
}
