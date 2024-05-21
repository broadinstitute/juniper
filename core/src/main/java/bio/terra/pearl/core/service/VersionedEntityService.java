package bio.terra.pearl.core.service;

import bio.terra.pearl.core.dao.BaseVersionedJdbiDao;
import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.Versioned;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class VersionedEntityService<T extends BaseEntity & Versioned, D extends BaseVersionedJdbiDao<T>>
        extends ImmutableEntityService<T, D> {

    public VersionedEntityService(D dao) {
        super(dao);
    }

    public Optional<T> findByStableId(String stableId, int version, UUID portalId) {
        return dao.findByStableId(stableId, version, portalId);
    }

    public Optional<T> findByStableIdAndPortalShortcode(String stableId, int version, String portalShortcode) {
        return dao.findByStableIdAndPortalShortcode(stableId, version, portalShortcode);
    }

    public List<T> findByStableId(String stableId, UUID portalId) {
        return dao.findByStableId(stableId, portalId);
    }

    public List<T> findByStableIdAndPortalShortcode(String stableId, String portalShortcode) {
        return dao.findByStableIdAndPortalShortcode(stableId, portalShortcode);
    }

    public List<T> findByStableIds(List<String> stableIds, List<Integer> versions, List<UUID> portalIds) {
        return dao.findByStableIds(stableIds, versions, portalIds);
    }

    public int getNextVersion(String stableId, UUID portalId) {
        return dao.getNextVersion(stableId, portalId);
    }

    public int getNextVersionByPortalShortcode(String stableId, String portalShortcode) {
        return dao.getNextVersionByPortalShortcode(stableId, portalShortcode);
    }

    @Transactional
    public void assignPublishedVersion(UUID id) {
        T entity = dao.find(id).get();
        if (entity.getPublishedVersion() != null) {
            // this survey already has a published version, do not reassign
            return;
        }
        int nextVersion = dao.getNextPublishedVersion(entity.getStableId(), entity.getPortalId());
        entity.setPublishedVersion(nextVersion);
        dao.setPublishedVersion(id, nextVersion);
    }

}
