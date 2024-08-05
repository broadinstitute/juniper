package bio.terra.pearl.api.admin.controller.forms;

import bio.terra.pearl.api.admin.api.ConfiguredSurveyApi;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyAuthContext;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.api.admin.service.forms.SurveyExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class ConfiguredSurveyController implements ConfiguredSurveyApi {
  private AuthUtilService authUtilService;
  private HttpServletRequest request;
  private ObjectMapper objectMapper;
  private SurveyExtService surveyExtService;

  public ConfiguredSurveyController(
      AuthUtilService authUtilService,
      HttpServletRequest request,
      ObjectMapper objectMapper,
      SurveyExtService surveyExtService) {
    this.authUtilService = authUtilService;
    this.request = request;
    this.objectMapper = objectMapper;
    this.surveyExtService = surveyExtService;
  }

  /**
   * gets all StudyEnvironmentSurveys for the given study, along with their associated Survey (minus
   * content)
   */
  @Override
  public ResponseEntity<Object> findWithNoContent(
      String portalShortcode,
      String studyShortcode,
      String envName,
      String stableId,
      String active) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName =
        envName != null ? EnvironmentName.valueOfCaseInsensitive(envName) : null;
    Boolean activeVal = active != null ? Boolean.valueOf(active) : null;
    List<StudyEnvironmentSurvey> studyEnvSurveys =
        surveyExtService.findWithSurveyNoContent(
            PortalStudyAuthContext.of(operator, portalShortcode, studyShortcode),
            stableId,
            environmentName,
            activeVal);
    return ResponseEntity.ok(studyEnvSurveys);
  }

  @Override
  public ResponseEntity<Object> patch(
      String portalShortcode, String studyShortcode, String envName, Object body) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    List<StudyEnvironmentSurvey> configuredSurveys =
        objectMapper.convertValue(body, new TypeReference<List<StudyEnvironmentSurvey>>() {});

    List<StudyEnvironmentSurvey> savedSes =
        surveyExtService.updateConfiguredSurveys(
            PortalStudyEnvAuthContext.of(
                operator, portalShortcode, studyShortcode, environmentName),
            configuredSurveys);
    return ResponseEntity.ok(savedSes);
  }

  @Override
  public ResponseEntity<Object> replace(
      String portalShortcode, String studyShortcode, String envName, Object body) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    StudyEnvironmentSurvey newStudyEnvSurvey =
        objectMapper.convertValue(body, StudyEnvironmentSurvey.class);
    newStudyEnvSurvey =
        surveyExtService.replace(
            PortalStudyEnvAuthContext.of(
                operator, portalShortcode, studyShortcode, environmentName),
            newStudyEnvSurvey);
    return ResponseEntity.ok(newStudyEnvSurvey);
  }

  @Override
  public ResponseEntity<Object> create(
      String portalShortcode, String studyShortcode, String envName, Object body) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    StudyEnvironmentSurvey configuredSurvey =
        objectMapper.convertValue(body, StudyEnvironmentSurvey.class);

    StudyEnvironmentSurvey savedSes =
        surveyExtService.createConfiguredSurvey(
            PortalStudyEnvAuthContext.of(
                operator, portalShortcode, studyShortcode, environmentName),
            configuredSurvey);
    return ResponseEntity.ok(savedSes);
  }

  @Override
  public ResponseEntity<Void> remove(
      String portalShortcode, String studyShortcode, String envName, UUID configuredSurveyId) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);

    surveyExtService.removeConfiguredSurvey(
        PortalStudyEnvAuthContext.of(operator, portalShortcode, studyShortcode, environmentName),
        configuredSurveyId);
    return ResponseEntity.noContent().build();
  }
}
