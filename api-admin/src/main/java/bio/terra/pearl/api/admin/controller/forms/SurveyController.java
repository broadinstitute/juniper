package bio.terra.pearl.api.admin.controller.forms;

import bio.terra.pearl.api.admin.api.SurveyApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.forms.SurveyExtService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.survey.Survey;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class SurveyController implements SurveyApi {
  private AuthUtilService authUtilService;
  private HttpServletRequest request;
  private SurveyExtService surveyExtService;
  private ObjectMapper objectMapper;

  public SurveyController(
      AuthUtilService authUtilService,
      HttpServletRequest request,
      SurveyExtService surveyExtService,
      ObjectMapper objectMapper) {
    this.authUtilService = authUtilService;
    this.request = request;
    this.surveyExtService = surveyExtService;
    this.objectMapper = objectMapper;
  }

  @Override
  public ResponseEntity<Object> get(String portalShortcode, String stableId, Integer version) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    Survey survey = surveyExtService.get(portalShortcode, stableId, version, adminUser);
    return ResponseEntity.ok(survey);
  }

  @Override
  public ResponseEntity<Object> getAllVersions(String portalShortcode, String stableId) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    return ResponseEntity.ok(surveyExtService.listVersions(portalShortcode, stableId, adminUser));
  }

  @Override
  public ResponseEntity<Object> create(String portalShortcode, Object body) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);

    Survey survey = objectMapper.convertValue(body, Survey.class);
    Survey savedSurvey = surveyExtService.create(portalShortcode, survey, adminUser);
    return ResponseEntity.ok(savedSurvey);
  }

  @Override
  public ResponseEntity<Object> newVersion(String portalShortcode, String stableId, Object body) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    Survey survey = objectMapper.convertValue(body, Survey.class);
    if (!stableId.equals(survey.getStableId())) {
      throw new IllegalArgumentException("survey parameters don't match");
    }
    Survey savedSurvey = surveyExtService.createNewVersion(portalShortcode, survey, adminUser);
    return ResponseEntity.ok(savedSurvey);
  }

  @Override
  public ResponseEntity<Void> delete(String portalShortcode, String stableId) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    surveyExtService.delete(portalShortcode, stableId, adminUser);
    return ResponseEntity.noContent().build();
  }
}
