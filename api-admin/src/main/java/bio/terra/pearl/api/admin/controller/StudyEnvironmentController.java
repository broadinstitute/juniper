package bio.terra.pearl.api.admin.controller;

import bio.terra.pearl.api.admin.api.StudyEnvironmentApi;
import bio.terra.pearl.api.admin.model.StudyEnvironmentDto;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.api.admin.service.study.StudyEnvironmentExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class StudyEnvironmentController implements StudyEnvironmentApi {
  private final AuthUtilService requestService;
  private final HttpServletRequest request;
  private final ObjectMapper objectMapper;
  private final StudyEnvironmentExtService studyEnvExtService;

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
    AdminUser operator = requestService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    StudyEnvironment envUpdate = objectMapper.convertValue(body, StudyEnvironment.class);
    StudyEnvironment savedEnv =
        studyEnvExtService.update(
            PortalStudyEnvAuthContext.of(
                operator, portalShortcode, studyShortcode, environmentName),
            envUpdate);
    return ResponseEntity.ok(objectMapper.convertValue(savedEnv, StudyEnvironmentDto.class));
  }

  /** updates the config object associated with the environment */
  @Override
  public ResponseEntity<Object> patchConfig(
      String portalShortcode, String studyShortcode, String envName, Object body) {
    AdminUser operator = requestService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    PortalStudyEnvAuthContext authContext =
        PortalStudyEnvAuthContext.of(operator, portalShortcode, studyShortcode, environmentName);
    StudyEnvironmentConfig configUpdate =
        objectMapper.convertValue(body, StudyEnvironmentConfig.class);
    StudyEnvironmentConfig savedConfig = studyEnvExtService.updateConfig(authContext, configUpdate);
    return ResponseEntity.ok(savedConfig);
  }

  @Override
  public ResponseEntity<Object> getKitTypes(
      String portalShortcode, String studyShortcode, String envName) {
    AdminUser adminUser = requestService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    PortalStudyEnvAuthContext authContext =
        PortalStudyEnvAuthContext.of(adminUser, portalShortcode, studyShortcode, environmentName);

    List<KitType> kitTypes = studyEnvExtService.getKitTypes(authContext);
    return ResponseEntity.ok(kitTypes);
  }

  @Override
  public ResponseEntity<Object> updateKitTypes(
      String portalShortcode, String studyShortcode, String envName, Object body) {
    AdminUser operator = requestService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    PortalStudyEnvAuthContext authContext =
        PortalStudyEnvAuthContext.of(operator, portalShortcode, studyShortcode, environmentName);

    List<String> kitTypeNames =
        objectMapper.convertValue(body, new TypeReference<List<String>>() {});

    studyEnvExtService.updateKitTypes(authContext, kitTypeNames);
    return ResponseEntity.ok(body);
  }

  @Override
  public ResponseEntity<Object> getAllowedKitTypes(
      String portalShortcode, String studyShortcode, String envName) {
    AdminUser operator = requestService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    PortalStudyEnvAuthContext authContext =
        PortalStudyEnvAuthContext.of(operator, portalShortcode, studyShortcode, environmentName);
    List<KitType> kitTypes = studyEnvExtService.getAllowedKitTypes(authContext);
    return ResponseEntity.ok(kitTypes);
  }

  @Override
  public ResponseEntity<Object> stats(
      String portalShortcode, String studyShortcode, String envName) {
    AdminUser operator = requestService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    PortalStudyEnvAuthContext authContext =
        PortalStudyEnvAuthContext.of(operator, portalShortcode, studyShortcode, environmentName);
    return ResponseEntity.ok(studyEnvExtService.getStats(authContext));
  }
}
