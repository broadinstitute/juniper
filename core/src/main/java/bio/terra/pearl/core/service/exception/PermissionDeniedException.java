package bio.terra.pearl.core.service.exception;

public class PermissionDeniedException extends ValidationException {
    public PermissionDeniedException(String message) {
        super(message);
    }
}
