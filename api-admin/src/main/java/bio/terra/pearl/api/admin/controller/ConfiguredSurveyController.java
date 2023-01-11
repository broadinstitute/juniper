package bio.terra.pearl.api.admin.controller;

import bio.terra.pearl.api.admin.api.ConfiguredSurveyApi;
import bio.terra.pearl.api.admin.model.ConfiguredSurveyDto;
import bio.terra.pearl.api.admin.service.RequestUtilService;
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
  private RequestUtilService requestService;
  private HttpServletRequest request;
  private ObjectMapper objectMapper;
  private StudyEnvironmentSurveyService studyEnvSurveyService;

  public ConfiguredSurveyController(
      RequestUtilService requestService,
      HttpServletRequest request,
      ObjectMapper objectMapper,
      StudyEnvironmentSurveyService studyEnvSurveyService) {
    this.requestService = requestService;
    this.request = request;
    this.objectMapper = objectMapper;
    this.studyEnvSurveyService = studyEnvSurveyService;
  }

  @Override
  public ResponseEntity<ConfiguredSurveyDto> patch(
      String portalShortcode,
      String studyShortcode,
      String envName,
      UUID configuredSurveyId,
      ConfiguredSurveyDto body) {
    AdminUser adminUser = requestService.getFromRequest(request);
    requestService.authUserToPortal(adminUser, portalShortcode);

    StudyEnvironmentSurvey configuredSurvey =
        objectMapper.convertValue(body, StudyEnvironmentSurvey.class);
    StudyEnvironmentSurvey existing = studyEnvSurveyService.find(configuredSurvey.getId()).get();
    BeanUtils.copyProperties(body, existing);
    StudyEnvironmentSurvey savedSes = studyEnvSurveyService.update(adminUser, existing);
    return ResponseEntity.ok(objectMapper.convertValue(savedSes, ConfiguredSurveyDto.class));
  }
}
