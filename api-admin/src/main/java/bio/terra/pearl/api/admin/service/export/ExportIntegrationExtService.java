package bio.terra.pearl.api.admin.service.export;

import bio.terra.pearl.api.admin.service.auth.EnforcePortalStudyEnvPermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.export.ExportIntegration;
import bio.terra.pearl.core.model.export.ExportIntegrationJob;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.export.integration.ExportIntegrationJobService;
import bio.terra.pearl.core.service.export.integration.ExportIntegrationService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ExportIntegrationExtService {
  private final ExportIntegrationService exportIntegrationService;

  private final ExportIntegrationJobService exportIntegrationJobService;

  public ExportIntegrationExtService(
      ExportIntegrationService exportIntegrationService,
      ExportIntegrationJobService exportIntegrationJobService) {
    this.exportIntegrationService = exportIntegrationService;
    this.exportIntegrationJobService = exportIntegrationJobService;
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
  public ExportIntegrationJob run(PortalStudyEnvAuthContext authContext, UUID id) {
    ExportIntegration integration =
        exportIntegrationService
            .findWithOptions(id)
            .orElseThrow(() -> new NotFoundException("Export Integration not found"));
    if (!integration.getStudyEnvironmentId().equals(authContext.getStudyEnvironment().getId())) {
      throw new NotFoundException("Export Integration not found");
    }
    ExportIntegrationJob job =
        exportIntegrationService.doExport(
            integration, new ResponsibleEntity(authContext.getOperator()));
    return job;
  }

  @EnforcePortalStudyEnvPermission(permission = "export_integration")
  public ExportIntegration create(
      PortalStudyEnvAuthContext authContext, ExportIntegration exportIntegration) {
    exportIntegration.setStudyEnvironmentId(authContext.getStudyEnvironment().getId());
    return exportIntegrationService.create(exportIntegration);
  }

  @EnforcePortalStudyEnvPermission(permission = "export_integration")
  public ExportIntegration save(
      PortalStudyEnvAuthContext authContext, ExportIntegration exportIntegration) {
    ExportIntegration loadedIntegration =
        exportIntegrationService
            .find(exportIntegration.getId())
            .orElseThrow(() -> new NotFoundException("Export Integration not found"));
    if (!loadedIntegration.getStudyEnvironmentId().equals(authContext.getStudyEnvironment().getId())
        || !loadedIntegration.getExportOptionsId().equals(exportIntegration.getExportOptionsId())) {
      throw new NotFoundException("Export Integration not found");
    }
    return exportIntegrationService.update(exportIntegration);
  }

  @EnforcePortalStudyEnvPermission(permission = "BASE")
  public List<ExportIntegrationJob> listJobs(PortalStudyEnvAuthContext authContext) {
    return exportIntegrationJobService.findByStudyEnvironment(
        authContext.getStudyEnvironment().getId());
  }
}
