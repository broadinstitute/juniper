package bio.terra.pearl.api.admin.service.export;

import bio.terra.pearl.api.admin.service.auth.EnforcePortalStudyEnvPermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.model.export.ExportIntegration;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.export.integration.ExportIntegrationService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ExportIntegrationExtService {
  private final ExportIntegrationService exportIntegrationService;

  public ExportIntegrationExtService(ExportIntegrationService exportIntegrationService) {
    this.exportIntegrationService = exportIntegrationService;
  }

  @EnforcePortalStudyEnvPermission(permission = "BASE")
  public List<ExportIntegration> list(PortalStudyEnvAuthContext authContext) {
    return exportIntegrationService.findByStudyEnvironmentId(
        authContext.getStudyEnvironment().getId());
  }

  @EnforcePortalStudyEnvPermission(permission = "BASE")
  public ExportIntegration find(PortalStudyEnvAuthContext authContext, UUID id) {
    ExportIntegration integration =
        exportIntegrationService
            .findWithOptions(id)
            .orElseThrow(() -> new NotFoundException("Export Integration not found"));
    if (!integration.getStudyEnvironmentId().equals(authContext.getStudyEnvironment().getId())) {
      throw new NotFoundException("Export Integration not found");
    }
    return integration;
  }

  @EnforcePortalStudyEnvPermission(permission = "participant_data_view")
  public ExportIntegration run(PortalStudyEnvAuthContext authContext, UUID id) {
    ExportIntegration integration =
        exportIntegrationService
            .findWithOptions(id)
            .orElseThrow(() -> new NotFoundException("Export Integration not found"));
    if (!integration.getStudyEnvironmentId().equals(authContext.getStudyEnvironment().getId())) {
      throw new NotFoundException("Export Integration not found");
    }
    exportIntegrationService.run(integration);
    return integration;
  }

  @EnforcePortalStudyEnvPermission(permission = "export_integration")
  public ExportIntegration create(
      PortalStudyEnvAuthContext authContext, ExportIntegration exportIntegration) {
    exportIntegration.setStudyEnvironmentId(authContext.getStudyEnvironment().getId());
    return exportIntegrationService.create(exportIntegration);
  }
}
