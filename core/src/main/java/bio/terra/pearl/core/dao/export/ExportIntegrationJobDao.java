package bio.terra.pearl.core.dao.export;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.export.ExportIntegrationJob;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ExportIntegrationJobDao extends BaseMutableJdbiDao<ExportIntegrationJob> {

    public ExportIntegrationJobDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<ExportIntegrationJob> getClazz() {
        return ExportIntegrationJob.class;
    }

    public List<ExportIntegrationJob> findByExportIntegrationId(UUID integrationId) {
        return findAllByProperty("export_integration_id", integrationId);
    }

    public List<ExportIntegrationJob> findByStudyEnvironmentId(UUID studyEnvironmentId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                            select %s from %s eij join export_integration
                             on eij.export_integration_id = export_integration.id 
                              where export_integration.study_environment_id = :studyEnvironmentId
                              """.formatted(prefixedGetQueryColumns("eij"), tableName))
                        .bind("studyEnvironmentId", studyEnvironmentId)
                        .mapTo(clazz)
                        .list()
        );
    }

    public void deleteByExportIntegrationId(UUID integrationId) {
        deleteByProperty("export_integration_id", integrationId);
    }

    public void deleteByExportIntegrationIds(List<UUID> integrationIds) {
        deleteByProperty("export_integration_id", integrationIds);
    }
}
