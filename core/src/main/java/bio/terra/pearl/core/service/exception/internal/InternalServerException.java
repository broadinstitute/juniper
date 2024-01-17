package bio.terra.pearl.core.service.exception.internal;

/** base class for exceptions that should result in 500 status code */
public class InternalServerException extends RuntimeException {
    public InternalServerException(String message) {
    super(message);
  }
  public InternalServerException(String message, Throwable cause) {
    super(message, cause);
  }
}
