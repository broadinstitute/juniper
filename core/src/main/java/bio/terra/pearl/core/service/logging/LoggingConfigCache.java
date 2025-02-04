package bio.terra.pearl.core.service.logging;

import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.service.portal.PortalEnvironmentConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * we don't want to have to go to the database every time an event is logged in our system to get the configs,
 * and they change very rarely. So we cache them here
 */
@Service
@Slf4j
public class LoggingConfigCache {
    private final PortalEnvironmentConfigService portalEnvironmentConfigService;

    public static final String CONFIGS_WITH_DOMAIN_CACHE_KEY = "portalEnvironmentConfigsWithDomain";

    public LoggingConfigCache(PortalEnvironmentConfigService portalEnvironmentConfigService) {
        this.portalEnvironmentConfigService = portalEnvironmentConfigService;
    }

    @Cacheable(value = CONFIGS_WITH_DOMAIN_CACHE_KEY)
    public Map<String, PortalEnvironmentConfig> getConfigsWithDomain() {
        return portalEnvironmentConfigService.findAllMappedByCustomDomain();
    }

    /** since we run multiple instances of the app, we can't rely on cache invalidation on writes,
     * so instead we just clear the cache every 10mins */
    @CacheEvict(allEntries = true, value = CONFIGS_WITH_DOMAIN_CACHE_KEY)
    @Scheduled(fixedDelay = 10 * 60 * 1000,  initialDelay = 10 * 60 * 1000)
    public void configCacheEvict() {
        log.info("Evicting cache for {}", CONFIGS_WITH_DOMAIN_CACHE_KEY);
    }
}
