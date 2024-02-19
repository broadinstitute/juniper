package bio.terra.pearl.core.service.address;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 *  Class for 400-500 responses returned from address validation service.
 */
@Getter
public class AddressValidationException extends RuntimeException {
    private HttpStatus httpStatusCode;

    public AddressValidationException(String message, HttpStatus httpStatusCode) {
        super(message);
        this.httpStatusCode = httpStatusCode;
    }
}
