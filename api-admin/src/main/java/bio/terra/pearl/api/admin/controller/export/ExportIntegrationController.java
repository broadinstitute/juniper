package bio.terra.pearl.api.admin.controller.export;

import bio.terra.pearl.api.admin.api.ExportIntegrationApi;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.api.admin.service.export.ExportIntegrationExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.export.ExportIntegration;
import bio.terra.pearl.core.model.export.ExportIntegrationJob;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class ExportIntegrationController implements ExportIntegrationApi {
  private final AuthUtilService authUtilService;
  private final HttpServletRequest request;
  private final ExportIntegrationExtService exportIntegrationExtService;
  private final ObjectMapper objectMapper;

  public ExportIntegrationController(
      AuthUtilService authUtilService,
      HttpServletRequest request,
      ExportIntegrationExtService exportIntegrationExtService,
      ObjectMapper objectMapper) {
    this.authUtilService = authUtilService;
    this.request = request;
    this.exportIntegrationExtService = exportIntegrationExtService;
    this.objectMapper = objectMapper;
  }

  @Override
  public ResponseEntity<Object> findByStudy(
      String portalShortcode, String studyShortcode, String envName) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    List<ExportIntegration> integrations =
        exportIntegrationExtService.list(
            PortalStudyEnvAuthContext.of(
                operator, portalShortcode, studyShortcode, environmentName));
    return ResponseEntity.ok(integrations);
  }

  @Override
  public ResponseEntity<Object> get(
      String portalShortcode, String studyShortcode, String envName, UUID id) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    ExportIntegration integration =
        exportIntegrationExtService.find(
            PortalStudyEnvAuthContext.of(
                operator, portalShortcode, studyShortcode, environmentName),
            id);
    return ResponseEntity.ok(integration);
  }

  @Override
  public ResponseEntity<Object> run(
      String portalShortcode, String studyShortcode, String envName, UUID id) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    ExportIntegrationJob job =
        exportIntegrationExtService.run(
            PortalStudyEnvAuthContext.of(
                operator, portalShortcode, studyShortcode, environmentName),
            id);
    return ResponseEntity.ok(job);
  }

  @Override
  public ResponseEntity<Object> create(
      String portalShortcode, String studyShortcode, String envName, Object body) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    ExportIntegration integration = objectMapper.convertValue(body, ExportIntegration.class);
    ExportIntegration newIntegration =
        exportIntegrationExtService.create(
            PortalStudyEnvAuthContext.of(
                adminUser, portalShortcode, studyShortcode, environmentName),
            integration);
    return ResponseEntity.ok(newIntegration);
  }

  @Override
  public ResponseEntity<Object> save(
      String portalShortcode, String studyShortcode, String envName, UUID id, Object body) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    ExportIntegration integration = objectMapper.convertValue(body, ExportIntegration.class);
    if (!id.equals(integration.getId())) {
      throw new IllegalArgumentException("ID in URL does not match ID in body");
    }
    integration =
        exportIntegrationExtService.save(
            PortalStudyEnvAuthContext.of(
                adminUser, portalShortcode, studyShortcode, environmentName),
            integration);
    return ResponseEntity.ok(integration);
  }

  @Override
  public ResponseEntity<Object> findJobsByStudy(
      String portalShortcode, String studyShortcode, String envName) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    List<ExportIntegrationJob> integrations =
        exportIntegrationExtService.listJobs(
            PortalStudyEnvAuthContext.of(
                operator, portalShortcode, studyShortcode, environmentName));
    return ResponseEntity.ok(integrations);
  }
}
