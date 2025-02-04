package bio.terra.pearl.core.service.portal;

import bio.terra.pearl.core.dao.portal.PortalEnvironmentConfigDao;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.model.publishing.ConfigChange;
import bio.terra.pearl.core.model.publishing.PortalEnvironmentChange;
import bio.terra.pearl.core.service.CrudService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import bio.terra.pearl.core.service.exception.internal.InternalServerException;
import bio.terra.pearl.core.service.publishing.PortalEnvPublishable;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class PortalEnvironmentConfigService extends CrudService<PortalEnvironmentConfig, PortalEnvironmentConfigDao> implements PortalEnvPublishable {
    public PortalEnvironmentConfigService(PortalEnvironmentConfigDao portalEnvironmentConfigDao) {
        super(portalEnvironmentConfigDao);
    }

    public Optional<PortalEnvironmentConfig> findByPortalEnvId(UUID portalEnvId) {
        return dao.findByPortalEnvId(portalEnvId);
    }

    public Map<String, PortalEnvironmentConfig> findAllMappedByCustomDomain() {
        return dao.findAllWithCustomDomain().stream()
                .collect(Collectors.toMap(PortalEnvironmentConfig::getParticipantHostname, Function.identity()));
    }

    public PortalEnvironmentConfig create(PortalEnvironmentConfig portalEnvironmentConfig) {
        return super.create(portalEnvironmentConfig);
    }

    public PortalEnvironmentConfig update(PortalEnvironmentConfig portalEnvironmentConfig) {
        return super.update(portalEnvironmentConfig);
    }

    @Override
    public void loadForPublishing(PortalEnvironment portalEnv) {
        if (portalEnv.getPortalEnvironmentConfigId() != null) {
            portalEnv.setPortalEnvironmentConfig(find(portalEnv.getPortalEnvironmentConfigId()).orElseThrow());
        }
    }

    @Override
    public void updateDiff(PortalEnvironmentChange change, PortalEnvironment sourceEnv, PortalEnvironment destEnv) {
        List<ConfigChange> envConfigChanges = ConfigChange.allChanges(sourceEnv.getPortalEnvironmentConfig(),
                destEnv.getPortalEnvironmentConfig(), getPublishIgnoreProps());
        change.setConfigChanges(envConfigChanges);
    }

    @Override
    public void applyDiff(PortalEnvironmentChange change, PortalEnvironment destEnv) {
        List<ConfigChange> configChanges = change.getConfigChanges();
        if (!configChanges.isEmpty()) {
            try {
                for (ConfigChange configChange : configChanges) {
                    PropertyUtils.setProperty(destEnv.getPortalEnvironmentConfig(), configChange.propertyName(), configChange.newValue());
                }
            } catch (Exception e) {
                throw new InternalServerException("Error copying properties during publish", e);
            }
            update(destEnv.getPortalEnvironmentConfig());
        }

    }
}
