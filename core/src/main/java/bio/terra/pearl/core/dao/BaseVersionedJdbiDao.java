package bio.terra.pearl.core.dao;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.Versioned;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jdbi.v3.core.Jdbi;

/** common dao for versioned entities. */
public abstract class BaseVersionedJdbiDao<T extends BaseEntity & Versioned> extends BaseJdbiDao<T> {
    public BaseVersionedJdbiDao(Jdbi jdbi) {
        super(jdbi);
    }

    public Optional<T> findByStableId(String stableId, int version) {
        return findByTwoProperties("stable_id", stableId, "version", version);
    }

    public List<T> findByStableId(String stableId) {
        return findAllByProperty("stable_id", stableId);
    }

    public int getNextVersion(String stableId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select max(version) from " + tableName + " where stable_id = :stableId")
                        .bind("stableId", stableId)
                        .mapTo(int.class)
                        .one()
        ) + 1;
    }

    public int getNextPublishedVersion(String stableId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select max(published_version) from " + tableName + " where stable_id = :stableId")
                        .bind("stableId", stableId)
                        .mapTo(int.class)
                        .one()
        ) + 1;
    }

    public void setPublishedVersion(UUID id, Integer publishedVersion) {
        BaseMutableJdbiDao.updateProperty(id, "published_version", publishedVersion, tableName, jdbi);
    }
}
