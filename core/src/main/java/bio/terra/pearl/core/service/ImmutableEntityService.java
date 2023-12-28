package bio.terra.pearl.core.service;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.BaseEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
public abstract class ImmutableEntityService<M extends BaseEntity, D extends BaseJdbiDao<M>> {
    protected D dao;
    protected final Logger logger;

    public ImmutableEntityService(D dao) {
        logger = log;
        this.dao = dao;
    }

    @Transactional
    public M create(M model) {
        return dao.create(model);
    }

    @Transactional
    public void bulkCreate(List<M> modelObjs) {
        dao.bulkCreate(modelObjs);
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

    /**
     * clears the id field, and resets the createdAt, and lastUpdatedAt fields of the object.
     * returns itself for easy chaining
     */
    public M cleanForCopying(M obj) {
        obj.setLastUpdatedAt(Instant.now());
        obj.setCreatedAt(Instant.now());
        obj.setId(null);
        return obj;
    }

}
