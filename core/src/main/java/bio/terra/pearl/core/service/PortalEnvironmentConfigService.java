package bio.terra.pearl.core.service;

import bio.terra.pearl.core.dao.PortalEnvironmentConfigDao;
import bio.terra.pearl.core.model.PortalEnvironmentConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortalEnvironmentConfigService {
    private PortalEnvironmentConfigDao portalEnvironmentConfigDao;

    public PortalEnvironmentConfigService(PortalEnvironmentConfigDao portalEnvironmentConfigDao) {
        this.portalEnvironmentConfigDao = portalEnvironmentConfigDao;
    }

    @Transactional
    public PortalEnvironmentConfig create(PortalEnvironmentConfig config) {
        return portalEnvironmentConfigDao.create(config);
    }
}
