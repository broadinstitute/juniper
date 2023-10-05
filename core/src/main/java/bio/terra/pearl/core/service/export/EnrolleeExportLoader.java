package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.dao.survey.AnswerDao;
import bio.terra.pearl.core.dao.survey.SurveyDao;
import bio.terra.pearl.core.dao.survey.SurveyQuestionDefinitionDao;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import bio.terra.pearl.core.service.participant.ProfileService;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import org.springframework.stereotype.Service;

@Service
public class EnrolleeExportLoader {
    private ProfileService profileService;
    private AnswerDao answerDao;
    private SurveyQuestionDefinitionDao surveyQuestionDefinitionDao;
    private SurveyResponseService surveyResponseService;
    private ParticipantTaskService participantTaskService;
    private SurveyDao surveyDao;

    public EnrolleeExportLoader(ProfileService profileService, AnswerDao answerDao,
                                SurveyQuestionDefinitionDao surveyQuestionDefinitionDao,
                                SurveyResponseService surveyResponseService,
                                ParticipantTaskService participantTaskService, SurveyDao surveyDao) {
        this.profileService = profileService;
        this.answerDao = answerDao;
        this.surveyQuestionDefinitionDao = surveyQuestionDefinitionDao;
        this.surveyResponseService = surveyResponseService;
        this.participantTaskService = participantTaskService;
        this.surveyDao = surveyDao;
    }
}
