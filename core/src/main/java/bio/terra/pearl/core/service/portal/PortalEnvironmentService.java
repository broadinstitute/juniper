package bio.terra.pearl.core.service.portal;

import bio.terra.pearl.core.dao.portal.PortalEnvironmentDao;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortalEnvironmentService {
    private PortalEnvironmentDao portalEnvironmentDao;
    private PortalEnvironmentConfigService portalEnvironmentConfigService;

    public PortalEnvironmentService(PortalEnvironmentDao portalEnvironmentDao,
                                    PortalEnvironmentConfigService portalEnvironmentConfigService) {
        this.portalEnvironmentDao = portalEnvironmentDao;
        this.portalEnvironmentConfigService = portalEnvironmentConfigService;
    }

    @Transactional
    public PortalEnvironment create(PortalEnvironment portalEnvironment) {
        PortalEnvironment newEnv = portalEnvironmentDao.create(portalEnvironment);
        PortalEnvironmentConfig envConfig = portalEnvironment.getPortalEnvironmentConfig();
        if (envConfig != null) {
            PortalEnvironmentConfig newConfig = portalEnvironmentConfigService.create(
                    portalEnvironment.getPortalEnvironmentConfig()
            );
            newEnv.setPortalEnvironmentConfig(newConfig);
            newEnv.setPortalEnvironmentConfigId(newConfig.getId());
        }
        return newEnv;
    }
}
