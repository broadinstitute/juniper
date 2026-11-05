package bio.terra.pearl.core.service.kit.sidr;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

/**
 * Thrown for error responses (4xx/5xx) from SIDR, or for responses that can't be processed. This intentionally extends
 * RuntimeException so that it can be thrown from within the `onStatus` handler of webClient.retrieve().
 * <p>
 * getMessage() includes the full raw SIDR response body and is meant for logs. Use getUserMessage() for anything
 * shown to study staff.
 */
@Getter
public class SIDRApiException extends RuntimeException {
    /** the error type SIDR uses in its response body for an address that EasyPost could not validate */
    public static final String ADDRESS_INVALID_TYPE = "invalid.address_invalid";
    /**
     * Marker included in the message of address validation failures. The admin UI looks for this to explain the
     * failure and offer the address validation override (same convention as the Pepper integration).
     */
    public static final String ADDRESS_VALIDATION_ERROR = "ADDRESS_VALIDATION_ERROR";

    private final HttpStatusCode httpStatusCode;
    /** the raw response body from SIDR, if any, for logging and troubleshooting */
    private final String responseBody;
    private final boolean addressValidationError;

    public SIDRApiException(String message) {
        this(message, null, null, null);
    }

    public SIDRApiException(String message, Throwable cause) {
        this(message, null, null, cause);
    }

    public SIDRApiException(String message, HttpStatusCode httpStatusCode, String responseBody, Throwable cause) {
        super(message, cause);
        this.httpStatusCode = httpStatusCode;
        this.responseBody = responseBody;
        this.addressValidationError = false;
    }

    private SIDRApiException(String message, HttpStatusCode httpStatusCode, String responseBody,
                             boolean addressValidationError) {
        super(message);
        this.httpStatusCode = httpStatusCode;
        this.responseBody = responseBody;
        this.addressValidationError = addressValidationError;
    }

    /** Builds an exception from an error response from SIDR. */
    public static SIDRApiException fromResponse(HttpStatusCode status, String body) {
        boolean addressInvalid = body != null && body.contains(ADDRESS_INVALID_TYPE);
        String loggedBody = body == null ? "" : body;
        String message = addressInvalid
                ? "%s: SIDR could not validate the address (%s)".formatted(ADDRESS_VALIDATION_ERROR, loggedBody)
                : "SIDR returned %s: %s".formatted(status, loggedBody);
        return new SIDRApiException(message, status, body, addressInvalid);
    }

    /**
     * A message that is safe to show to study staff: it never includes the SIDR response body.
     */
    public String getUserMessage() {
        if (addressValidationError) {
            return ADDRESS_VALIDATION_ERROR + ": SIDR could not validate the participant's address";
        }
        return httpStatusCode == null
                ? "Unable to communicate with SIDR. Please try again or contact support."
                : "SIDR could not process the kit request (HTTP %d). Please try again or contact support."
                .formatted(httpStatusCode.value());
    }
}
