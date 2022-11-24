package bio.terra.pearl.core.service;

import bio.terra.pearl.core.dao.EnvironmentConfigDao;
import bio.terra.pearl.core.model.EnvironmentConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EnvironmentConfigService {
    @Autowired
    private EnvironmentConfigDao environmentConfigDao;

    @Transactional
    public EnvironmentConfig create(EnvironmentConfig config) {
        return environmentConfigDao.create(config);
    }
}
