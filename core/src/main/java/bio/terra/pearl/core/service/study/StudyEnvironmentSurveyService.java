package bio.terra.pearl.core.service.study;

import bio.terra.pearl.core.dao.study.StudyEnvironmentSurveyDao;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.service.CrudService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyEnvironmentSurveyService extends CrudService<StudyEnvironmentSurvey, StudyEnvironmentSurveyDao> {
    public StudyEnvironmentSurveyService(StudyEnvironmentSurveyDao dao) {
        super(dao);
    }

    public List<StudyEnvironmentSurvey> findAllByStudyEnvId(UUID studyEnvId, Boolean active) {
        return dao.findAllByStudyEnvironmentId(studyEnvId, active);
    }


    public List<StudyEnvironmentSurvey> findAllByStudyEnvIdWithSurvey(UUID studyEnvId) {
        return dao.findAllByStudyEnvIdWithSurvey(studyEnvId, true);
    }

    public List<StudyEnvironmentSurvey> findAllByStudyEnvIdWithSurvey(UUID studyEnvId, Boolean active) {
        return dao.findAllByStudyEnvIdWithSurvey(studyEnvId, active);
    }

    public Optional<StudyEnvironmentSurvey> findActiveBySurvey(UUID studyEnvId, UUID surveyId) {
        return dao.findActiveBySurvey(studyEnvId, surveyId);
    }

    @Transactional
    public StudyEnvironmentSurvey deactivate(UUID id) {
        StudyEnvironmentSurvey ses = dao.find(id).get();
        ses.setActive(false);
        return dao.update(ses);
    }

    public Optional<StudyEnvironmentSurvey> findActiveBySurvey(UUID studyEnvId, String stableId) {
        List<StudyEnvironmentSurvey> configs = dao.findActiveBySurvey(studyEnvId, stableId);
        // we don't yet have robust support for having multiple surveys with the same stableId configured for an
        // environment.  For now, just pick one
        return configs.stream().findFirst();
    }

    public List<StudyEnvironmentSurvey> findBySurveyId(UUID surveyId) {
        return dao.findBySurveyId(surveyId);
    }

    public void deleteBySurveyId(UUID surveyId) {
        dao.deleteBySurveyId(surveyId);
    }

}
