package bio.terra.pearl.core.factory.survey;

import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SurveyResponseFactory {
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private SurveyFactory surveyFactory;
    @Autowired
    private SurveyResponseService surveyResponseService;
    @Autowired
    private AnswerFactory answerFactory;

    public SurveyResponse.SurveyResponseBuilder builder(String testName) {
        return SurveyResponse.builder().complete(false);
    }

    public SurveyResponse.SurveyResponseBuilder builderWithDependencies(String testName) {
        Survey survey = surveyFactory.buildPersisted(testName);
        Enrollee enrollee = enrolleeFactory.buildPersisted(testName);
        return builder(testName)
                .enrolleeId(enrollee.getId())
                .creatingParticipantUserId(enrollee.getParticipantUserId())
                .surveyId(survey.getId());
    }

    public SurveyResponse buildPersisted(String testName) {
        return surveyResponseService.create(builderWithDependencies(testName).build());
    }

    public SurveyResponse buildWithAnswers(Enrollee enrollee, Survey survey, Map<String, Object> answerMap) {
        SurveyResponse response = surveyResponseService.create(SurveyResponse.builder()
                .enrolleeId(enrollee.getId())
                .creatingParticipantUserId(enrollee.getParticipantUserId())
                .surveyId(survey.getId())
                .build());
        List<Answer> answers = answerFactory.createFromMap(answerMap, enrollee, survey, response);
        response.setAnswers(answers);
        return response;
    }
}
