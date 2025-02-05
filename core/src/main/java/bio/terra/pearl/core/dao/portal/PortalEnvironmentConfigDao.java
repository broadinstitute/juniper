package bio.terra.pearl.core.dao.portal;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class PortalEnvironmentConfigDao extends BaseMutableJdbiDao<PortalEnvironmentConfig> {
    public PortalEnvironmentConfigDao(Jdbi jdbi) {
        super(jdbi);
    }
    @Override
    protected Class<PortalEnvironmentConfig> getClazz() {
        return PortalEnvironmentConfig.class;
    }

    public Optional<PortalEnvironmentConfig> findByPortalEnvId(UUID portalEnvId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                                select a.* from %s a
                                join portal_environment on portal_environment_config_id = a.id
                                where portal_environment.id = :portalEnvId
                                """.formatted(tableName))
                        .bind("portalEnvId", portalEnvId)
                        .mapTo(clazz)
                        .findOne()
        );
    }

    public List<PortalEnvironmentConfig> findAllWithCustomDomain() {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                                select * from %s where participant_hostname is not null
                                """.formatted(tableName))
                        .mapTo(clazz)
                        .list()
        );
    }
}
