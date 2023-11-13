package bio.terra.pearl.api.participant.controller;

import bio.terra.common.exception.BadRequestException;
import bio.terra.common.exception.InternalServerErrorException;
import bio.terra.common.exception.UnauthorizedException;
import bio.terra.common.exception.ValidationException;
import bio.terra.pearl.api.participant.model.ErrorReport;
import com.auth0.jwt.exceptions.JWTDecodeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler({
    MethodArgumentNotValidException.class,
    MethodArgumentTypeMismatchException.class,
    IllegalArgumentException.class,
    NoHandlerFoundException.class,
    ValidationException.class,
    BadRequestException.class
  })
  public static ResponseEntity<ErrorReport> badRequestExceptionHandler(Exception ex) {
    return buildErrorReport(ex, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler({
    JWTDecodeException.class,
  })
  public static ResponseEntity<ErrorReport> authenticationExceptionHandler(Exception ex) {
    return buildErrorReport(ex, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler({
    UnauthorizedException.class,
  })
  public static ResponseEntity<ErrorReport> authorizationExceptionHandler(Exception ex) {
    return buildErrorReport(ex, HttpStatus.FORBIDDEN);
  }

  // catchall - internal server error
  @ExceptionHandler({InternalServerErrorException.class, Exception.class})
  public static ResponseEntity<ErrorReport> internalErrorExceptionHandler(Exception ex) {
    log.error("Exception caught by catch-all handler", ex);
    return buildErrorReport(ex, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  protected static ResponseEntity<ErrorReport> buildErrorReport(
      Throwable ex, HttpStatus statusCode) {
    StringBuilder causes = new StringBuilder("Exception: ").append(ex);
    for (Throwable cause = ex.getCause(); cause != null; cause = cause.getCause()) {
      causes.append("\nCause: ").append(cause);
    }
    log.info("Global exception handler: " + causes, ex);

    return new ResponseEntity<>(
        new ErrorReport().message(ex.getMessage()).statusCode(statusCode.value()), statusCode);
  }
}
