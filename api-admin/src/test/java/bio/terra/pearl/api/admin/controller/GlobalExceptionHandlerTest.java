package bio.terra.pearl.api.admin.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import bio.terra.pearl.api.admin.model.ErrorReport;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Slf4j
public class GlobalExceptionHandlerTest {

  @Test
  void testBuildErrorReport() {
    Exception nestedException = new Exception("base exception", new Exception("root cause"));
    GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();
    ResponseEntity<ErrorReport> errorReport =
        globalExceptionHandler.buildErrorReport(nestedException, HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(errorReport.getStatusCode(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR));
    assertThat(errorReport.getBody().getMessage(), equalTo("base exception"));
    log.info("Global exception handler: " + errorReport);
  }
}
