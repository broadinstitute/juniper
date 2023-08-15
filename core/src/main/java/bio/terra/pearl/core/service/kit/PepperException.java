package bio.terra.pearl.core.service.kit;

import lombok.Getter;

/**
 * Runtime exception thrown when there's an error with a request to Pepper. Note: This very intentionally extends
 * RuntimeException in order to auto-trigger rollback when it escapes a @Transactional method. If this was a checked
 * exception, it would need to be called out in @Transactional's rollbackFor where it might be thrown to prevent
 * committing the transaction for a failed operation. This is extra code/configuration that is difficult to test, so
 * instead we'll choose to not "fight the framework".
 */
@Getter
public class PepperException extends RuntimeException {
    private PepperErrorResponse errorResponse;

    public PepperException(String message) {
        super(message);
    }

    public PepperException(String message, Exception cause) {
        super(message, cause);
    }

    public PepperException(String message, PepperErrorResponse errorResponse) {
        super("%s for kit %s: %s".formatted(
                message,
                errorResponse.getJuniperKitId(),
                errorResponse.getErrorMessage()
        ));
        this.errorResponse = errorResponse;
    }
}
