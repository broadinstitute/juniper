package bio.terra.pearl.core.dao.study;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.dao.survey.SurveyDao;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class StudyEnvironmentSurveyDao extends BaseJdbiDao<StudyEnvironmentSurvey> {
    private SurveyDao surveyDao;
    public StudyEnvironmentSurveyDao(Jdbi jdbi, SurveyDao surveyDao) {
        super(jdbi);
        this.surveyDao = surveyDao;
    }

    @Override
    protected Class<StudyEnvironmentSurvey> getClazz() {
        return StudyEnvironmentSurvey.class;
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvId) {
        deleteByUuidProperty("study_environment_id", studyEnvId);
    }

    public List<StudyEnvironmentSurvey> findAllByStudyEnvironmentId(UUID studyEnvId) {
        return findAllByPropertySorted("study_environment_id", studyEnvId,
                "survey_order", "ASC");
    }

    /** gets all the study environment surveys and attaches the relevant survey objects in a batch */
    public List<StudyEnvironmentSurvey> findAllByStudyEnvIdWithSurvey(UUID studyEnvId) {
        List<StudyEnvironmentSurvey> studyEnvSurvs = findAllByStudyEnvironmentId(studyEnvId);
        List<UUID> surveyIds = studyEnvSurvs.stream().map(ses -> ses.getSurveyId()).collect(Collectors.toList());
        List<Survey> surveys = surveyDao.findAllById(surveyIds);
        for (StudyEnvironmentSurvey ses : studyEnvSurvs) {
            ses.setSurvey(surveys.stream().filter(survey -> survey.getId().equals(ses.getSurveyId()))
                    .findFirst().get());
        }
        return studyEnvSurvs;
    }
}
