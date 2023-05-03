package bio.terra.pearl.api.admin.controller.forms;

import bio.terra.pearl.api.admin.api.ConfiguredSurveyApi;
import bio.terra.pearl.api.admin.model.ConfiguredSurveyDto;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.forms.SurveyExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
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
  public ResponseEntity<ConfiguredSurveyDto> patch(
      String portalShortcode,
      String studyShortcode,
      String envName,
      UUID configuredSurveyId,
      ConfiguredSurveyDto body) {
    AdminUser adminUser = requestService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    StudyEnvironmentSurvey configuredSurvey =
        objectMapper.convertValue(body, StudyEnvironmentSurvey.class);

    StudyEnvironmentSurvey savedSes = surveyExtService
        .updateConfiguredSurvey(portalShortcode, environmentName, configuredSurvey, adminUser);
    return ResponseEntity.ok(objectMapper.convertValue(savedSes, ConfiguredSurveyDto.class));
  }
}
