package bio.terra.pearl.api.admin.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bio.terra.pearl.api.admin.controller.study.StudyController;
import bio.terra.pearl.api.admin.model.ErrorReport;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.study.StudyExtService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.kit.StudyEnvironmentKitTypeService;
import bio.terra.pearl.core.service.site.SiteContentService;
import bio.terra.pearl.core.service.study.PortalStudyService;
import bio.terra.pearl.core.service.study.StudyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@Slf4j
@ContextConfiguration(classes = StudyController.class)
@WebMvcTest
public class GlobalExceptionHandlerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @SpyBean private StudyExtService studyExtService;
  @MockBean private StudyService studyService;
  @MockBean private AuthUtilService authUtilService;
  @MockBean private StudyEnvironmentKitTypeService studyEnvironmentKitTypeService;
  @MockBean private PortalStudyService portalStudyService;
  @MockBean private SiteContentService siteContentService;
  @Autowired private StudyController studyController;
  @SpyBean private GlobalExceptionHandler globalExceptionHandler;

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

  @Test
  void testStudyCreateThrowsPsqlException() throws Exception {
    AdminUser adminUser = AdminUser.builder().superuser(true).build();
    when(authUtilService.requireAdminUser(any())).thenReturn(adminUser);
    when(authUtilService.authUserToPortal(any(), any())).thenReturn(null);

    doThrow(new RuntimeException("Message", new PSQLException("foo", PSQLState.UNKNOWN_STATE)))
        .when(studyService)
        .create(any());

    String portalShortCode = "portal1";
    StudyExtService.StudyCreationDto studyCreationDto =
        new StudyExtService.StudyCreationDto("studyShortCode", "studyName");
    MockHttpServletRequestBuilder request =
        post("/api/portals/v1/%s/studies".formatted(portalShortCode))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(studyCreationDto));
    ResultActions result = mockMvc.perform(request);

    result.andExpect(status().is5xxServerError());
    // result.andExpectAll(
    //     status().is5xxServerError(), content().string(containsString("PsqlException")));
  }
}
