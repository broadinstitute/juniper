package bio.terra.pearl.core.service;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.BaseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public abstract class CrudService<M extends BaseEntity, D extends BaseJdbiDao<M>> {
    protected D dao;
    public CrudService(D dao) {
        this.dao = dao;
    }

    @Transactional
    public M create(M model) {
        return dao.create(model);
    }

    public Optional<M> find(UUID id) {
        return dao.find(id);
    }

    public int count() { return dao.count(); }

    @Transactional
    public void delete(UUID id, Set<CascadeProperty> cascade) {
        dao.delete(id);
    }
}
