package bio.terra.pearl.api.admin.controller;

import bio.terra.pearl.api.admin.api.ConfiguredSurveyApi;
import bio.terra.pearl.api.admin.model.ConfiguredSurveyDto;
import bio.terra.pearl.api.admin.service.RequestUtilService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.survey.SurveyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class ConfiguredSurveyController implements ConfiguredSurveyApi {
  private RequestUtilService requestService;
  private HttpServletRequest request;
  private SurveyService surveyService;
  private ObjectMapper objectMapper;
  private StudyEnvironmentSurveyService sesService;

  public ConfiguredSurveyController(
      RequestUtilService requestService,
      HttpServletRequest request,
      SurveyService surveyService,
      ObjectMapper objectMapper,
      StudyEnvironmentSurveyService sesService) {
    this.requestService = requestService;
    this.request = request;
    this.surveyService = surveyService;
    this.objectMapper = objectMapper;
    this.sesService = sesService;
  }

  @Override
  public ResponseEntity<ConfiguredSurveyDto> patch(
      String portalShortcode,
      String studyShortcode,
      String envName,
      String configuredSurveyId,
      ConfiguredSurveyDto body) {
    AdminUser adminUser = requestService.getFromRequest(request);
    Portal portal = requestService.authUserToPortal(adminUser, portalShortcode);
    StudyEnvironmentSurvey configuredSurvey =
        objectMapper.convertValue(body, StudyEnvironmentSurvey.class);
    StudyEnvironmentSurvey match = sesService.find(configuredSurvey.getId()).get();
    // TODO -- eventually this endpoint should support updating other fields of SES
    match.setSurveyId(configuredSurvey.getSurveyId());
    StudyEnvironmentSurvey savedSes = sesService.update(adminUser, match);
    return ResponseEntity.ok(objectMapper.convertValue(savedSes, ConfiguredSurveyDto.class));
  }
}
