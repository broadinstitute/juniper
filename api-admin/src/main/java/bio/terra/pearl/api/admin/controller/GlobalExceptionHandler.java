package bio.terra.pearl.api.admin.controller;

import bio.terra.common.exception.AbstractGlobalExceptionHandler;
import bio.terra.pearl.api.admin.model.ErrorReport;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler extends AbstractGlobalExceptionHandler<ErrorReport> {

  @Override
  public ErrorReport generateErrorReport(Throwable ex, HttpStatus statusCode, List<String> causes) {
    return new ErrorReport().message(ex.getMessage()).statusCode(statusCode.value());
  }
}
