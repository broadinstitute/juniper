package bio.terra.pearl.core.service;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.BaseEntity;
import org.springframework.transaction.annotation.Transactional;

public abstract class CrudService<M extends BaseEntity, D extends BaseMutableJdbiDao<M>> extends ImmutableEntityService <M, D> {
    public CrudService(D dao) {
        super(dao);
    }

    @Transactional
    public M update(M entity) {
        return dao.update(entity);
    }
}
