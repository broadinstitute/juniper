package bio.terra.pearl.api.admin.controller;

import bio.terra.pearl.api.admin.api.SurveyApi;
import bio.terra.pearl.api.admin.model.VersionedFormDto;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.survey.SurveyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class SurveyController implements SurveyApi {
  private AuthUtilService requestService;
  private HttpServletRequest request;
  private SurveyService surveyService;
  private ObjectMapper objectMapper;

  public SurveyController(
      AuthUtilService requestService,
      HttpServletRequest request,
      SurveyService surveyService,
      ObjectMapper objectMapper) {
    this.requestService = requestService;
    this.request = request;
    this.surveyService = surveyService;
    this.objectMapper = objectMapper;
  }

  @Override
  public ResponseEntity<VersionedFormDto> newVersion(
      String portalShortcode, String stableId, VersionedFormDto body) {
    AdminUser adminUser = requestService.requireAdminUser(request);
    Portal portal = requestService.authUserToPortal(adminUser, portalShortcode);
    if (!stableId.equals(body.getStableId())) {
      throw new IllegalArgumentException("survey parameters don't match");
    }
    Survey survey = objectMapper.convertValue(body, Survey.class);

    Survey savedSurvey = surveyService.createNewVersion(adminUser, portal.getId(), survey);

    VersionedFormDto savedSurveyDto =
        objectMapper.convertValue(savedSurvey, VersionedFormDto.class);
    return ResponseEntity.ok(savedSurveyDto);
  }
}
