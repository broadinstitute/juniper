package bio.terra.pearl.core.service.address;

import bio.terra.pearl.core.service.kit.pepper.PepperErrorResponse;
import org.springframework.http.HttpStatusCode;

/**
 *  Class for 400-500 responses returned from address validation service.  This intentionally extends
 *  RuntimeException in order to be correctly handled within `onStatus` of webClient.retrieve().
 *  See e.g. https://medium.com/nerd-for-tech/webclient-error-handling-made-easy-4062dcf58c49
 */
public class AddressValidationException extends RuntimeException {
    private PepperErrorResponse errorResponse;
    private HttpStatusCode httpStatusCode;

    public AddressValidationException(String message, HttpStatusCode httpStatusCode) {
        super(message);
        this.httpStatusCode = httpStatusCode;
        this.errorResponse = null;
    }
}
