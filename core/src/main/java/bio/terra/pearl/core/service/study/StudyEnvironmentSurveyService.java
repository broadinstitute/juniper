package bio.terra.pearl.core.service.study;

import bio.terra.pearl.core.dao.study.StudyEnvironmentSurveyDao;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.service.CrudService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class StudyEnvironmentSurveyService extends CrudService<StudyEnvironmentSurvey, StudyEnvironmentSurveyDao> {
    public StudyEnvironmentSurveyService(StudyEnvironmentSurveyDao dao) {
        super(dao);
    }

    public List<StudyEnvironmentSurvey> findAllByStudyEnvIdWithSurvey(UUID studyEnvId) {
        return dao.findAllByStudyEnvIdWithSurvey(studyEnvId);
    }

    public Optional<StudyEnvironmentSurvey> findBySurvey(UUID studyEnvId, UUID surveyId) {
        return dao.findBySurvey(studyEnvId, surveyId);
    }

    public Optional<StudyEnvironmentSurvey> findBySurvey(UUID studyEnvId, String stableId) {
        var configs = dao.findBySurvey(studyEnvId, stableId);
        // we don't yet have robust support for having multiple surveys with the same stableId configured for an
        // environment.  For now, just pick one
        return configs.stream().findFirst();
    }

    public List<StudyEnvironmentSurvey> findAllStudyEnvsWithSurveyId(UUID surveyId) {
        return dao.findAllStudyEnvsWithSurveyId(surveyId);
    }

}
