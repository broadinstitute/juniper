package bio.terra.pearl.api.admin.controller;

import bio.terra.pearl.api.admin.api.SurveyApi;
import bio.terra.pearl.api.admin.model.SurveyDto;
import bio.terra.pearl.api.admin.service.RequestUtilService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.survey.SurveyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class SurveyController implements SurveyApi {
  private RequestUtilService requestService;
  private HttpServletRequest request;
  private SurveyService surveyService;
  private ObjectMapper objectMapper;

  public SurveyController(
      RequestUtilService requestService,
      HttpServletRequest request,
      SurveyService surveyService,
      PortalService portalService,
      ObjectMapper objectMapper) {
    this.requestService = requestService;
    this.request = request;
    this.surveyService = surveyService;
    this.objectMapper = objectMapper;
  }

  @Override
  public ResponseEntity<SurveyDto> publish(
      String portalShortcode, String stableId, Integer version, SurveyDto body) {
    AdminUser adminUser = requestService.getFromRequest(request);
    Portal portal = requestService.authUserToPortal(adminUser, portalShortcode);
    if (!stableId.equals(body.getStableId()) || !body.getVersion().equals(version)) {
      throw new IllegalArgumentException("survey parameters don't match");
    }
    Survey survey = objectMapper.convertValue(body, Survey.class);

    Survey savedSurvey = surveyService.createNewVersion(adminUser, portal.getId(), survey);

    SurveyDto savedSurveyDto = objectMapper.convertValue(savedSurvey, SurveyDto.class);
    return ResponseEntity.ok(savedSurveyDto);
  }
}
