package bio.terra.pearl.core.service.kit.pepper;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

/**
 *  Class for 400-500 responses returned from Pepper.  This intentionally extends
 *  RuntimeException in order to be correctly handled within `onStatus` of webClient.retrieve().
 *  See e.g. https://medium.com/nerd-for-tech/webclient-error-handling-made-easy-4062dcf58c49
 */
@Getter
public class PepperApiException extends RuntimeException {
    private PepperErrorResponse errorResponse;
    private HttpStatusCode httpStatusCode;

    public PepperApiException(String message) {
        super(message);
    }

    public PepperApiException(String message, Exception cause) {
        super(message, cause);
    }

    public PepperApiException(String message, PepperErrorResponse errorResponse, HttpStatusCode httpStatusCode) {
        super(message);
        this.httpStatusCode = httpStatusCode;
        this.errorResponse = errorResponse;
    }

    public PepperApiException(String message, HttpStatusCode httpStatusCode) {
        super(message);
        this.httpStatusCode = httpStatusCode;
        this.errorResponse = null;
    }
}
