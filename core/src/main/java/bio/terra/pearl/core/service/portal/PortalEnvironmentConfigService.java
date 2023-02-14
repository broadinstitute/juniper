package bio.terra.pearl.core.service.portal;

import bio.terra.pearl.core.dao.portal.PortalEnvironmentConfigDao;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.service.CrudService;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PortalEnvironmentConfigService extends CrudService<PortalEnvironmentConfig, PortalEnvironmentConfigDao> {

    public PortalEnvironmentConfigService(PortalEnvironmentConfigDao portalEnvironmentConfigDao) {
        super(portalEnvironmentConfigDao);
    }

    public Optional<PortalEnvironmentConfig> findByPortalEnvId(UUID portalEnvId) {
        return dao.findByPortalEnvId(portalEnvId);
    }
}
