package bio.terra.pearl.core.service;

import bio.terra.pearl.core.dao.survey.PreregistrationResponseDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.PreregistrationResponse;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.survey.SurveyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {
    private SurveyService surveyService;
    private StudyEnvironmentService studyEnvService;
    private PreregistrationResponseDao preregistrationResponseDao;

    public RegistrationService(SurveyService surveyService,
                               StudyEnvironmentService studyEnvService,
                               PreregistrationResponseDao preregistrationResponseDao) {
        this.surveyService = surveyService;
        this.studyEnvService = studyEnvService;
        this.preregistrationResponseDao = preregistrationResponseDao;
    }

    /** Creates a preregistration survey record for a user who is not signed in */
    @Transactional
    public PreregistrationResponse createAnonymousPreregistration(
            String portalShortcode,
            EnvironmentName envName,
            String studyShortcode,
            String surveyStableId,
            Integer surveyVersion,
            String fullData) {
        PreregistrationResponse response = new PreregistrationResponse();
        Survey survey = surveyService.findByStableId(surveyStableId, surveyVersion).get();
        StudyEnvironment studyEnv = studyEnvService.findByStudy(studyShortcode, envName).get();

        response.setSurveyId(survey.getId());
        response.setStudyEnvironmentId(studyEnv.getId());
        response.setFullData(fullData);
        return preregistrationResponseDao.create(response);
    }
}
