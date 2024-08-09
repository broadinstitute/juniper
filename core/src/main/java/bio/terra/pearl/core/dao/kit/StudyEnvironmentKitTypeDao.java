package bio.terra.pearl.core.dao.kit;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.kit.StudyEnvironmentKitType;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class StudyEnvironmentKitTypeDao extends BaseMutableJdbiDao<StudyEnvironmentKitType> {
    public StudyEnvironmentKitTypeDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<StudyEnvironmentKitType> getClazz() {
        return StudyEnvironmentKitType.class;
    }

    public List<StudyEnvironmentKitType> findByStudyEnvironmentId(UUID studyEnvId) {
        return findAllByProperty("study_environment_id", studyEnvId);
    }

    public void deleteByKitTypeIdAndStudyEnvironmentId(UUID kitTypeId, UUID studyEnvId) {
        deleteByTwoProperties("kit_type_id", kitTypeId, "study_environment_id", studyEnvId);
    }
}
