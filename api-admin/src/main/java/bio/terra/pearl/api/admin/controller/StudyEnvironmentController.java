package bio.terra.pearl.api.admin.controller;

import bio.terra.pearl.api.admin.api.StudyEnvironmentApi;
import bio.terra.pearl.api.admin.model.StudyEnvironmentDto;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.study.StudyEnvironmentExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.List;

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
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    StudyEnvironment envUpdate = objectMapper.convertValue(body, StudyEnvironment.class);
    StudyEnvironment savedEnv =
        studyEnvExtService.update(
            adminUser, portalShortcode, studyShortcode, environmentName, envUpdate);
    return ResponseEntity.ok(objectMapper.convertValue(savedEnv, StudyEnvironmentDto.class));
  }

  /** updates the config object associated with the environment */
  @Override
  public ResponseEntity<Object> patchConfig(
      String portalShortcode, String studyShortcode, String envName, Object body) {
    AdminUser adminUser = requestService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    StudyEnvironmentConfig configUpdate =
        objectMapper.convertValue(body, StudyEnvironmentConfig.class);
    StudyEnvironmentConfig savedConfig =
        studyEnvExtService.updateConfig(
            adminUser, portalShortcode, studyShortcode, environmentName, configUpdate);
    return ResponseEntity.ok(savedConfig);
  }

  @Override
  public ResponseEntity<Object> getKitTypes(
      String portalShortcode, String studyShortcode, String envName) {
    AdminUser adminUser = requestService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
      List<KitType> kitTypes =
        studyEnvExtService.getKitTypes(adminUser, portalShortcode, studyShortcode, environmentName);
    return ResponseEntity.ok(kitTypes);
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
