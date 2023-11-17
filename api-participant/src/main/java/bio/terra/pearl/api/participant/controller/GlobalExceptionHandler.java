package bio.terra.pearl.api.participant.controller;

import bio.terra.common.exception.BadRequestException;
import bio.terra.common.exception.InternalServerErrorException;
import bio.terra.common.exception.UnauthorizedException;
import bio.terra.common.exception.ValidationException;
import bio.terra.pearl.api.participant.model.ErrorReport;
import com.auth0.jwt.exceptions.JWTDecodeException;
import javax.servlet.http.HttpServletRequest;
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

  private final HttpServletRequest request;

  public GlobalExceptionHandler(HttpServletRequest request) {
    this.request = request;
  }

  @ExceptionHandler({
    MethodArgumentNotValidException.class,
    MethodArgumentTypeMismatchException.class,
    IllegalArgumentException.class,
    NoHandlerFoundException.class,
    ValidationException.class,
    BadRequestException.class
  })
  public ResponseEntity<ErrorReport> badRequestExceptionHandler(Exception ex) {
    return buildErrorReport(ex, HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler({
    JWTDecodeException.class,
  })
  public ResponseEntity<ErrorReport> authenticationExceptionHandler(Exception ex) {
    return buildErrorReport(ex, HttpStatus.UNAUTHORIZED, request);
  }

  @ExceptionHandler({
    UnauthorizedException.class,
  })
  public ResponseEntity<ErrorReport> authorizationExceptionHandler(Exception ex) {
    return buildErrorReport(ex, HttpStatus.FORBIDDEN, request);
  }

  // catchall - internal server error
  @ExceptionHandler({InternalServerErrorException.class, Exception.class})
  public ResponseEntity<ErrorReport> internalErrorExceptionHandler(Exception ex) {
    return buildErrorReport(ex, HttpStatus.INTERNAL_SERVER_ERROR, request);
  }

  protected static ResponseEntity<ErrorReport> buildErrorReport(
      Throwable ex, HttpStatus statusCode, HttpServletRequest request) {

    StringBuilder causes = new StringBuilder("Exception: ").append(ex);
    for (Throwable cause = ex.getCause(); cause != null; cause = cause.getCause()) {
      causes.append("\nCause: ").append(cause);
    }

    String logString =
        String.format(
            "%s%nRequest: %s %s %s",
            causes, request.getMethod(), request.getRequestURI(), statusCode.value());

    switch (statusCode) {
      case INTERNAL_SERVER_ERROR:
        log.error(logString, ex);
        break;
      case BAD_REQUEST, UNAUTHORIZED, FORBIDDEN, NOT_FOUND:
      default:
        log.info(logString, ex);
        break;
    }

    return new ResponseEntity<>(
        new ErrorReport().message(ex.getMessage()).statusCode(statusCode.value()), statusCode);
  }
}
