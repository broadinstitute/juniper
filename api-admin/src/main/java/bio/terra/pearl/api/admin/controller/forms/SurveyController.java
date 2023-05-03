package bio.terra.pearl.api.admin.controller.forms;

import bio.terra.pearl.api.admin.api.SurveyApi;
import bio.terra.pearl.api.admin.model.VersionedFormDto;
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
  private AuthUtilService requestService;
  private HttpServletRequest request;
  private SurveyExtService surveyExtService;
  private ObjectMapper objectMapper;

  public SurveyController(
      AuthUtilService requestService,
      HttpServletRequest request,
      SurveyExtService surveyExtService,
      ObjectMapper objectMapper) {
    this.requestService = requestService;
    this.request = request;
    this.surveyExtService = surveyExtService;
    this.objectMapper = objectMapper;
  }

  @Override
  public ResponseEntity<VersionedFormDto> newVersion(
      String portalShortcode, String stableId, VersionedFormDto body) {
    AdminUser adminUser = requestService.requireAdminUser(request);
    if (!stableId.equals(body.getStableId())) {
      throw new IllegalArgumentException("survey parameters don't match");
    }
    Survey survey = objectMapper.convertValue(body, Survey.class);
    Survey savedSurvey = surveyExtService.createNewVersion(portalShortcode, survey, adminUser);
    VersionedFormDto savedSurveyDto =
        objectMapper.convertValue(savedSurvey, VersionedFormDto.class);
    return ResponseEntity.ok(savedSurveyDto);
  }
}
