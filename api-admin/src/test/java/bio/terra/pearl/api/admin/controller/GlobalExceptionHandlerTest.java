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
import bio.terra.pearl.api.admin.models.dto.StudyCreationDto;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.forms.SurveyExtService;
import bio.terra.pearl.api.admin.service.study.StudyExtService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.kit.StudyEnvironmentKitTypeService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.site.SiteContentService;
import bio.terra.pearl.core.service.study.PortalStudyService;
import bio.terra.pearl.core.service.study.StudyService;
import bio.terra.pearl.populate.service.FilePopulateService;
import bio.terra.pearl.populate.service.StudyPopulator;
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
import org.springframework.mock.web.MockHttpServletRequest;
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
  @MockBean private PortalService portalService;
  @MockBean private StudyPopulator studyPopulator;
  @MockBean private FilePopulateService filePopulateService;

  @Test
  void testBuildErrorReport() {
    Exception nestedException = new Exception("base exception", new Exception("root cause"));
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setMethod("POST");
    request.setRequestURI("/api/portals/v1/portal1/studies");
    ResponseEntity<ErrorReport> errorReport =
        GlobalExceptionHandler.buildErrorReport(nestedException, HttpStatus.BAD_REQUEST, request);
    assertThat(errorReport.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    assertThat(errorReport.getBody().getMessage(), equalTo("base exception"));

    // we do not expose internal error exception messages to the client
    errorReport =
        GlobalExceptionHandler.buildErrorReport(
            nestedException, HttpStatus.INTERNAL_SERVER_ERROR, request);
    assertThat(errorReport.getStatusCode(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR));
    assertThat(errorReport.getBody().getMessage(), equalTo("Internal server error"));
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
    StudyCreationDto studyCreationDto = new StudyCreationDto("studyShortCode", "studyName");
    MockHttpServletRequestBuilder request =
        post("/api/portals/v1/%s/studies".formatted(portalShortCode))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(studyCreationDto));
    ResultActions result = mockMvc.perform(request);

    result.andExpect(status().is5xxServerError());
  }
}
