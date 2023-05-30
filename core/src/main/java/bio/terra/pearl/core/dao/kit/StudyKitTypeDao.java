package bio.terra.pearl.core.dao.kit;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.kit.StudyKitType;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class StudyKitTypeDao extends BaseMutableJdbiDao<StudyKitType> {
    public StudyKitTypeDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<StudyKitType> getClazz() {
        return StudyKitType.class;
    }

    public List<StudyKitType> findByStudyId(UUID studyId) {
        return findAllByProperty("study_id", studyId);
    }
}
