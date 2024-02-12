package bio.terra.pearl.core.service.address;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

/**
 *  Class for 400-500 responses returned from address validation service.
 */
@Getter
public class AddressValidationException extends RuntimeException {
    private HttpStatusCode httpStatusCode;

    public AddressValidationException(String message, HttpStatusCode httpStatusCode) {
        super(message);
        this.httpStatusCode = httpStatusCode;
    }
}
