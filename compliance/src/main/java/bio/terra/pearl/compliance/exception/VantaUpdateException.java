package bio.terra.pearl.compliance.exception;

/**
 * Exception thrown when we can't update vanta metadata
 */
public class VantaUpdateException extends RuntimeException {

    public VantaUpdateException(String message) {
        super(message);
    }

}
