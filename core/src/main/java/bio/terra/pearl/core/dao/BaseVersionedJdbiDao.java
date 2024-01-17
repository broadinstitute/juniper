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

    public List<T> findByStableIds(List<String> stableIds, List<Integer> versions) {
        return findAllByTwoProperties("stable_id", stableIds, "version", versions);
    }

    public List<T> findByStableId(String stableId) {
        return findAllByProperty("stable_id", stableId);
    }

    /** gets 1 plus the previous highest version, or 1 if no version already exists */
    public int getNextVersion(String stableId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select max(version) from " + tableName + " where stable_id = :stableId")
                        .bind("stableId", stableId)
                        .mapTo(int.class)
                        .one()
        ) + 1;
    }

    /** gets 1 plus the previous highest version, or 1 if no published version already exists */
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
