package bio.terra.pearl.core.service;

import bio.terra.pearl.core.dao.BaseVersionedJdbiDao;
import bio.terra.pearl.core.dao.survey.SurveyDao;
import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.Versioned;
import bio.terra.pearl.core.model.survey.Survey;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class VersionedEntityService<T extends BaseEntity & Versioned, D extends BaseVersionedJdbiDao<T>>
        extends ImmutableEntityService<T, D> {

    public VersionedEntityService(D dao) {
        super(dao);
    }
    public Optional<T> findByStableId(String stableId, int version) {
        return dao.findByStableId(stableId, version);
    }

    public List<T> findByStableId(String stableId) {
        return dao.findByStableId(stableId);
    }

    public List<T> findByStableIds(List<String> stableIds, List<Integer> versions) {
        return dao.findByStableIds(stableIds, versions);
    }

    public int getNextVersion(String stableId) {
        return dao.getNextVersion(stableId);
    }

    @Transactional
    public void assignPublishedVersion(UUID id) {
        T entity = dao.find(id).get();
        if (entity.getPublishedVersion() != null) {
            // this survey already has a published version, do not reassign
            return;
        }
        int nextVersion = dao.getNextPublishedVersion(entity.getStableId());
        entity.setPublishedVersion(nextVersion);
        dao.setPublishedVersion(id, nextVersion);
    }

}
