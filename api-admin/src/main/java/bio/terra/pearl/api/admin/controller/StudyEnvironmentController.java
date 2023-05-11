package bio.terra.pearl.api.admin.controller;

import bio.terra.pearl.api.admin.api.StudyEnvironmentApi;
import bio.terra.pearl.api.admin.model.StudyEnvironmentDto;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.StudyEnvironmentExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class StudyEnvironmentController implements StudyEnvironmentApi {
  private AuthUtilService requestService;
  private HttpServletRequest request;
  private ObjectMapper objectMapper;
  private StudyEnvironmentExtService studyEnvExtService;

  public StudyEnvironmentController(
      AuthUtilService requestService,
      HttpServletRequest request,
      ObjectMapper objectMapper,
      StudyEnvironmentExtService studyEnvExtService) {
    this.requestService = requestService;
    this.request = request;
    this.objectMapper = objectMapper;
    this.studyEnvExtService = studyEnvExtService;
  }

  /** currently only supports updating the preEnroll survey id */
  @Override
  public ResponseEntity<StudyEnvironmentDto> patch(
      String portalShortcode, String studyShortcode, String envName, StudyEnvironmentDto body) {
    AdminUser adminUser = requestService.requireAdminUser(request);
    StudyEnvironment envUpdate = objectMapper.convertValue(body, StudyEnvironment.class);
    StudyEnvironment savedEnv =
        studyEnvExtService.update(adminUser, portalShortcode, studyShortcode, envUpdate);
    return ResponseEntity.ok(objectMapper.convertValue(savedEnv, StudyEnvironmentDto.class));
  }

  @Override
  public ResponseEntity<Object> stats(
      String portalShortcode, String studyShortcode, String envName) {
    AdminUser adminUser = requestService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    return ResponseEntity.ok(
        studyEnvExtService.getStats(adminUser, portalShortcode, studyShortcode, environmentName));
  }
}
