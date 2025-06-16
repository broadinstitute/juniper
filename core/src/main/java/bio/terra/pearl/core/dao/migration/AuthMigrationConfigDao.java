package bio.terra.pearl.core.dao.migration;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.migration.AuthMigrationConfig;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AuthMigrationConfigDao extends BaseMutableJdbiDao<AuthMigrationConfig> {
    public AuthMigrationConfigDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<AuthMigrationConfig> getClazz() { return AuthMigrationConfig.class; }


    public Optional<AuthMigrationConfig> findByStudyEnvId(UUID studyEnvId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select * " +
                        " from " + tableName + " auth_migration_config amc " +
                        " inner join study_environment_config sec on amc.study_environment_config_id = sec.id" +
                        " where sec.study_environment_id = :studyEnvId" +
                        " order by amc.created_at desc limit 1")
                        .bind("studyEnvId", studyEnvId)
                        .mapTo(clazz)
                        .findFirst()
        );
    }


    public Optional<AuthMigrationConfig> findByStudyEnvConfigId(UUID studyEnvConfigId) {
        return findByProperty("study_environment_config_id", studyEnvConfigId);
    }
}
