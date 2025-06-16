package bio.terra.pearl.core.service.migration;

import bio.terra.pearl.core.dao.migration.AuthMigrationConfigDao;
import bio.terra.pearl.core.model.migration.AuthMigrationConfig;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthMigrationConfigService extends CrudService<AuthMigrationConfig, AuthMigrationConfigDao> {
    public AuthMigrationConfigService(AuthMigrationConfigDao dao) {
        super(dao);
    }

    public Optional<AuthMigrationConfig> findByStudyEnvId(UUID studyEnvId) {
        return dao.findByStudyEnvId(studyEnvId);
    }

    public Optional<AuthMigrationConfig> findByStudyEnvConfigId(UUID studyEnvConfigId) {
        return dao.findByStudyEnvConfigId(studyEnvConfigId);
    }

}
