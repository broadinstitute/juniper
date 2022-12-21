package bio.terra.pearl.core.service.exception;

/** general exception class for errors due to not finding the specified resource */
public class NotFoundException extends ValidationException {
    public NotFoundException(String message) {
        super(message);
    }
}
