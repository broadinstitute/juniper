package bio.terra.pearl.api.admin.controller.forms;

import bio.terra.pearl.api.admin.api.SurveyResponseApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.forms.SurveyResponseExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.model.workflow.HubResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class SurveyResponseController implements SurveyResponseApi {
  private final AuthUtilService authUtilService;
  private final HttpServletRequest request;
  private final ObjectMapper objectMapper;
  private final SurveyResponseExtService surveyResponseExtService;

  public SurveyResponseController(
      AuthUtilService authUtilService,
      HttpServletRequest request,
      ObjectMapper objectMapper,
      SurveyResponseExtService surveyResponseExtService) {
    this.authUtilService = authUtilService;
    this.request = request;
    this.objectMapper = objectMapper;
    this.surveyResponseExtService = surveyResponseExtService;
  }

  @Override
  public ResponseEntity<Object> update(
      String portalShortcode,
      String envName,
      String studyShortcode,
      String enrolleeShortcode,
      String stableId,
      Integer version,
      UUID taskId,
      Object body) {
    AdminUser user = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    SurveyResponse responseDto = objectMapper.convertValue(body, SurveyResponse.class);
    HubResponse hubResponse =
        surveyResponseExtService.updateResponse(
            user, portalShortcode, environmentName, responseDto, enrolleeShortcode, taskId);
    return ResponseEntity.ok(hubResponse);
  }
}
