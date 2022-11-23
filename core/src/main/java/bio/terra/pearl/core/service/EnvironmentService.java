package bio.terra.pearl.core.service;

import bio.terra.pearl.core.dao.EnvironmentDao;
import bio.terra.pearl.core.model.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EnvironmentService {
    private EnvironmentDao environmentDao;
    public EnvironmentService(EnvironmentDao environmentDao) {
        this.environmentDao = environmentDao;
    }

    @Transactional
    public Environment create(Environment environment) {
        return environmentDao.create(environment);
    }
}
