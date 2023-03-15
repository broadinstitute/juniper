package bio.terra.pearl.core.service;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.BaseEntity;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

public abstract class CrudService<M extends BaseEntity, D extends BaseJdbiDao<M>> {
    protected D dao;
    protected final Logger logger;

    public CrudService(D dao) {
        logger = LoggerFactory.getLogger(this.getClass());
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

    public List<M> findAll() { return dao.findAll(); }
    public List<M> findAll(List<UUID> uuids) { return dao.findAll(uuids); }

    @Transactional
    public void delete(UUID id, Set<CascadeProperty> cascade) {
        dao.delete(id);
    }
}
