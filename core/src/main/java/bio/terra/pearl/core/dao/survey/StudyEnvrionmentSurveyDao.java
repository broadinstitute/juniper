package bio.terra.pearl.core.dao.survey;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StudyEnvrionmentSurveyDao extends BaseJdbiDao<StudyEnvironmentSurvey> {

    public StudyEnvrionmentSurveyDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<StudyEnvironmentSurvey> getClazz() {
        return StudyEnvironmentSurvey.class;
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvId) {
        deleteByUuidProperty("study_environment_id", studyEnvId);
    }
}
