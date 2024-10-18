package bio.terra.pearl.core.dao;

import bio.terra.pearl.core.dao.JdbiDao;
import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.study.StudyEnvAttached;

import java.util.List;
import java.util.UUID;

public interface StudyEnvAttachedDao<T extends BaseEntity & StudyEnvAttached> extends JdbiDao<T> {
    default List<T> findByStudyEnvironmentId(UUID studyEnvId) {
        return getDao().findAllByProperty("study_environment_id", studyEnvId);
    }

    default void deleteByStudyEnvironmentId(UUID studyEnvId) {
        getDao().deleteByProperty("study_environment_id", studyEnvId);
    }

    default int countByStudyEnvironmentId(UUID studyEnvId) {
        return getDao().countByProperty("study_environment_id", studyEnvId);
    }
}
