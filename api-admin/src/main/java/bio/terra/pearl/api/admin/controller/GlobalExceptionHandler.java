package bio.terra.pearl.api.admin.controller;

import bio.terra.common.exception.BadRequestException;
import bio.terra.common.exception.InternalServerErrorException;
import bio.terra.common.exception.NotFoundException;
import bio.terra.common.exception.UnauthorizedException;
import bio.terra.common.exception.ValidationException;
import bio.terra.pearl.api.admin.model.ErrorReport;
import bio.terra.pearl.core.service.address.AddressValidationException;
import bio.terra.pearl.core.service.exception.ExceptionUtils;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
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
    BadRequestException.class,
    HttpMessageNotReadableException.class,
    MissingServletRequestParameterException.class
  })
  public ResponseEntity<ErrorReport> badRequestExceptionHandler(Exception ex) {
    return buildErrorReport(ex, HttpStatus.BAD_REQUEST, request);
  }

  public static ResponseEntity<ErrorReport> badRequestHandler(
      Exception ex, HttpServletRequest request) {
    return buildErrorReport(ex, HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler({
    JWTDecodeException.class,
  })
  public ResponseEntity<ErrorReport> authenticationExceptionHandler(Exception ex) {
    return buildErrorReport(ex, HttpStatus.UNAUTHORIZED, request);
  }

  @ExceptionHandler({UnauthorizedException.class, PermissionDeniedException.class})
  public ResponseEntity<ErrorReport> authorizationExceptionHandler(Exception ex) {
    return buildErrorReport(ex, HttpStatus.FORBIDDEN, request);
  }

  @ExceptionHandler({
    NotFoundException.class,
    jakarta.ws.rs.NotFoundException.class,
    bio.terra.pearl.core.service.exception.NotFoundException.class,
    HttpRequestMethodNotSupportedException.class
  })
  public ResponseEntity<ErrorReport> notFoundExceptionHandler(Exception ex) {
    return buildErrorReport(ex, HttpStatus.NOT_FOUND, request);
  }

  // catchall - internal server error
  @ExceptionHandler({InternalServerErrorException.class, Exception.class})
  public ResponseEntity<ErrorReport> internalErrorExceptionHandler(Exception ex) {
    return buildErrorReport(ex, HttpStatus.INTERNAL_SERVER_ERROR, request);
  }

  @ExceptionHandler({AddressValidationException.class})
  public ResponseEntity<ErrorReport> addressValidationExceptionHandler(
      AddressValidationException ex) {
    return buildErrorReport(ex, ex.getHttpStatusCode(), request);
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

    ExceptionUtils.truncateIfNeeded(ex);

    String message;
    if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR) {
      log.error(logString, ex);
      // don't share internal error messages with the client
      message = "Internal server error";
    } else {
      log.info(logString, ex);
      message = ex.getMessage();
    }

    return new ResponseEntity<>(
        new ErrorReport()
            .errorClass(ex.getClass().getName())
            .message(message)
            .statusCode(statusCode.value()),
        statusCode);
  }
}
