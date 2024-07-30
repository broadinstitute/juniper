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
        return dao.findAll(List.of(studyEnvId), null, active);
    }


    public List<StudyEnvironmentSurvey> findAllByStudyEnvIdWithSurvey(UUID studyEnvId) {
        return dao.findAllWithSurvey(studyEnvId, true);
    }

    public List<StudyEnvironmentSurvey> findAllByStudyEnvIdWithSurvey(UUID studyEnvId, Boolean active) {
        return dao.findAllWithSurvey(studyEnvId, active);
    }

    public List<StudyEnvironmentSurvey> findAllByStudyEnvIdWithSurveyNoContent(UUID studyEnvId, Boolean active) {
        return dao.findAllWithSurveyNoContent(List.of(studyEnvId), null, active);
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

    @Transactional
    @Override
    public StudyEnvironmentSurvey create(StudyEnvironmentSurvey studyEnvSurvey) {
        validateSurveyNotAlreadyActive(studyEnvSurvey);
        return super.create(studyEnvSurvey);
    }

    @Transactional
    @Override
    public StudyEnvironmentSurvey update(StudyEnvironmentSurvey studyEnvSurvey) {
        validateSurveyNotAlreadyActive(studyEnvSurvey);
        return super.update(studyEnvSurvey);
    }

    public void validateSurveyNotAlreadyActive(StudyEnvironmentSurvey studyEnvSurvey) {
        if (studyEnvSurvey.isActive() &&
            dao.isSurveyActiveInEnv(studyEnvSurvey.getSurveyId(), studyEnvSurvey.getStudyEnvironmentId(), studyEnvSurvey.getId())
        ) {
            throw new IllegalArgumentException("Cannot save -- another version of the survey is already active, likely due to multiple saves overlapping.  Confirm no one else is working on the survey and then retry.");
        }
    }

    public List<StudyEnvironmentSurvey> findActiveBySurvey(UUID studyEnvId, String stableId) {
        return dao.findActiveBySurvey(studyEnvId, stableId);
    }

    public List<StudyEnvironmentSurvey> findBySurveyId(UUID surveyId) {
        return dao.findBySurveyId(surveyId);
    }

    public List<StudyEnvironmentSurvey> findAllWithSurveyNoContent(List<UUID> studyEnvIds, String stableId, Boolean active) {
        return dao.findAllWithSurveyNoContent(studyEnvIds, stableId, active);
    }

    public void deleteBySurveyId(UUID surveyId) {
        dao.deleteBySurveyId(surveyId);
    }

}
