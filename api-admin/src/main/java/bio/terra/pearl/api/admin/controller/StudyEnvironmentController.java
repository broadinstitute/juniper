package bio.terra.pearl.api.admin.controller;

import bio.terra.pearl.api.admin.api.StudyEnvironmentApi;
import bio.terra.pearl.api.admin.model.StudyEnvironmentDto;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class StudyEnvironmentController implements StudyEnvironmentApi {
  private AuthUtilService requestService;
  private HttpServletRequest request;
  private ObjectMapper objectMapper;
  private StudyEnvironmentService studyEnvService;

  public StudyEnvironmentController(
      AuthUtilService requestService,
      HttpServletRequest request,
      ObjectMapper objectMapper,
      StudyEnvironmentService studyEnvService) {
    this.requestService = requestService;
    this.request = request;
    this.objectMapper = objectMapper;
    this.studyEnvService = studyEnvService;
  }

  /** currently only supports updating the preEnroll survey id */
  @Override
  public ResponseEntity<StudyEnvironmentDto> patch(
      String portalShortcode, String studyShortcode, String envName, StudyEnvironmentDto body) {
    AdminUser adminUser = requestService.requireAdminUser(request);
    requestService.authUserToPortal(adminUser, portalShortcode);

    StudyEnvironment existing = studyEnvService.find(body.getId()).get();
    existing.setPreEnrollSurveyId(body.getPreEnrollSurveyId());
    StudyEnvironment savedStudyEnv = studyEnvService.update(existing);
    return ResponseEntity.ok(objectMapper.convertValue(savedStudyEnv, StudyEnvironmentDto.class));
  }
}
