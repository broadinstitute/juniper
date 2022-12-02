package bio.terra.pearl.core.service;

import bio.terra.pearl.core.dao.EnvironmentDao;
import bio.terra.pearl.core.model.Environment;
import bio.terra.pearl.core.model.EnvironmentName;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class EnvironmentService {
    private EnvironmentDao environmentDao;
    public EnvironmentService(EnvironmentDao environmentDao) {
        this.environmentDao = environmentDao;
    }

    @Transactional
    public Environment create(Environment environment) {
        return environmentDao.create(environment);
    }

    public Optional<Environment> findOneByName(EnvironmentName name) {
        return environmentDao.findByName(name);
    }
}
