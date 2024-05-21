package bio.terra.pearl.core.dao;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.Versioned;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** common dao for versioned entities. */
public abstract class BaseVersionedJdbiDao<T extends BaseEntity & Versioned> extends BaseJdbiDao<T> {
    public BaseVersionedJdbiDao(Jdbi jdbi) {
        super(jdbi);
    }

    public Optional<T> findByStableId(String stableId, int version, UUID portalId) {
        return findByThreeProperties("stable_id", stableId, "version", version, "portal_id", portalId);
    }

    public List<T> findByStableIds(List<String> stableIds, List<Integer> versions, List<UUID> portalIds) {
        return findAllByThreeProperties("stable_id", stableIds, "version", versions, "portal_id", portalIds);
    }

    public List<T> findByStableId(String stableId, UUID portalId) {
        return findAllByTwoProperties("stable_id", stableId, "portal_id", portalId);
    }

    /** gets 1 plus the previous highest version, or 1 if no version already exists */
    public int getNextVersion(String stableId, UUID portalId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select max(version) from " + tableName + " where stable_id = :stableId and portal_id = :portalId")
                        .bind("stableId", stableId)
                        .bind("portalId", portalId)
                        .mapTo(int.class)
                        .one()
        ) + 1;
    }

    /** gets 1 plus the previous highest version, or 1 if no published version already exists */
    public int getNextPublishedVersion(String stableId, UUID portalId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select max(published_version) from " + tableName + " where stable_id = :stableId and portal_id = :portalId")
                        .bind("stableId", stableId)
                        .bind("portalId", portalId)
                        .mapTo(int.class)
                        .one()
        ) + 1;
    }

    public void setPublishedVersion(UUID id, Integer publishedVersion) {
        BaseMutableJdbiDao.updateProperty(id, "published_version", publishedVersion, tableName, jdbi);
    }

    public List<T> findByStableIdAndPortalShortcode(String stableId, String portalShortcode) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select * from " + tableName + " where stable_id = :stableId and portal_id = (select id from portal where shortcode = :shortcode)")
                        .bind("stableId", stableId)
                        .bind("shortcode", portalShortcode)
                        .mapTo(clazz)
                        .list()
        );
    }

    public Optional<T> findByStableIdAndPortalShortcode(String stableId, int version, String portalShortcode) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select * from " + tableName + " where stable_id = :stableId and portal_id = (select id from portal where shortcode = :shortcode) and version = :version")
                        .bind("stableId", stableId)
                        .bind("shortcode", portalShortcode)
                        .bind("version", version)
                        .mapTo(clazz)
                        .findOne()
        );
    }

    public int getNextVersionByPortalShortcode(String stableId, String portalShortcode) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select max(version) from " + tableName + " where stable_id = :stableId and portal_id = (select id from portal where shortcode = :shortcode)")
                        .bind("stableId", stableId)
                        .bind("shortcode", portalShortcode)
                        .mapTo(int.class)
                        .one()
        ) + 1;
    }
}
