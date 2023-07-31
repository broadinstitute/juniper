package bio.terra.pearl.api.admin.controller.forms;

import bio.terra.pearl.api.admin.api.ConfiguredSurveyApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.forms.SurveyExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class ConfiguredSurveyController implements ConfiguredSurveyApi {
  private AuthUtilService requestService;
  private HttpServletRequest request;
  private ObjectMapper objectMapper;
  private SurveyExtService surveyExtService;

  public ConfiguredSurveyController(
      AuthUtilService requestService,
      HttpServletRequest request,
      ObjectMapper objectMapper,
      SurveyExtService surveyExtService) {
    this.requestService = requestService;
    this.request = request;
    this.objectMapper = objectMapper;
    this.surveyExtService = surveyExtService;
  }

  @Override
  public ResponseEntity<Object> patch(
      String portalShortcode,
      String studyShortcode,
      String envName,
      UUID configuredSurveyId,
      Object body) {
    AdminUser adminUser = requestService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    StudyEnvironmentSurvey configuredSurvey =
        objectMapper.convertValue(body, StudyEnvironmentSurvey.class);

    StudyEnvironmentSurvey savedSes =
        surveyExtService.updateConfiguredSurvey(
            portalShortcode, environmentName, configuredSurvey, adminUser);
    return ResponseEntity.ok(savedSes);
  }

  @Override
  public ResponseEntity<Object> create(
      String portalShortcode, String studyShortcode, String envName, Object body) {
    AdminUser adminUser = requestService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    StudyEnvironmentSurvey configuredSurvey =
        objectMapper.convertValue(body, StudyEnvironmentSurvey.class);

    StudyEnvironmentSurvey savedSes =
        surveyExtService.createConfiguredSurvey(
            portalShortcode, studyShortcode, environmentName, configuredSurvey, adminUser);
    return ResponseEntity.ok(savedSes);
  }
}
