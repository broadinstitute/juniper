package bio.terra.pearl.core.service.exception.internal;

public class IOInternalException extends InternalServerException {
    public IOInternalException(String message) {
        super(message);
    }
    public IOInternalException(String message, Throwable cause) {
        super(message, cause);
    }
}
