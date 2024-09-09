package bio.terra.pearl.compliance.exception;

public class VantaUpdateException extends RuntimeException {

    public VantaUpdateException(String message) {
        super(message);
    }

    public VantaUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
