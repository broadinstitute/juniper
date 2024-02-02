package bio.terra.pearl.core.service;

import bio.terra.pearl.core.dao.EnvironmentDao;
import bio.terra.pearl.core.model.Environment;
import bio.terra.pearl.core.model.EnvironmentName;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnvironmentService extends ImmutableEntityService<Environment, EnvironmentDao> {

    public EnvironmentService(EnvironmentDao dao) {
        super(dao);
    }

    public Optional<Environment> findOneByName(EnvironmentName name) {
        return dao.findByName(name);
    }

}
