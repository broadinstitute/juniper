package bio.terra.pearl.core.service.kit.sidr;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

/**
 * Thrown for error responses (4xx/5xx) from SIDR, or for responses that can't be processed. This intentionally extends
 * RuntimeException so that it can be thrown from within the `onStatus` handler of webClient.retrieve().
 * <p>
 * The message of this exception is returned to study staff, so it must never include SIDR response content or
 * anything about our credentials, and should be plain language that doesn't assume the reader knows which system we order kits from. The raw SIDR response is available from getResponseBody(), and is logged by
 * the client; underlying errors belong in the cause.
 */
@Getter
public class SIDRApiException extends RuntimeException {
    /** the error type SIDR uses in its response body for an address that EasyPost could not validate */
    public static final String ADDRESS_INVALID_TYPE = "invalid.address_invalid";

    private final HttpStatusCode httpStatusCode;
    /** the raw response body from SIDR, if any. For logging and troubleshooting only, never show to users */
    private final String responseBody;
    private final boolean addressValidationError;

    /** @param message must be safe to show to study staff */
    public SIDRApiException(String message) {
        this(message, null, null, false, null);
    }

    /** @param message must be safe to show to study staff */
    public SIDRApiException(String message, Throwable cause) {
        this(message, null, null, false, cause);
    }

    private SIDRApiException(String message, HttpStatusCode httpStatusCode, String responseBody,
                             boolean addressValidationError, Throwable cause) {
        super(message, cause);
        this.httpStatusCode = httpStatusCode;
        this.responseBody = responseBody;
        this.addressValidationError = addressValidationError;
    }

    /** Builds an exception from an error response from SIDR. */
    public static SIDRApiException fromResponse(HttpStatusCode status, String body) {
        if (body != null && body.contains(ADDRESS_INVALID_TYPE)) {
            return new SIDRApiException("Could not validate the participant's address",
                    status, body, true, null);
        }
        return new SIDRApiException(
                "Could not place kit order (HTTP %d). Please try again or contact support."
                        .formatted(status.value()),
                status, body, false, null);
    }
}
