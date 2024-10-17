package bio.terra.pearl.core.factory.survey;

import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.survey.*;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
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

    /** to create a response with an objectValued answer, use a JsonNode value in the answerMap */
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

    public HubResponse<SurveyResponse> submitStringAnswer(ParticipantTask task,
                                                          String questionStableId,
                                                          String answerValue,
                                                          boolean complete,
                                                          EnrolleeBundle bundle,
                                                          Portal portal) {
        SurveyResponse response = SurveyResponse.builder()
                .answers(List.of(
                        Answer.builder()
                                .questionStableId(questionStableId)
                                .answerType(AnswerType.STRING)
                                .stringValue(answerValue).build()))
                .complete(complete)
                .build();

        return surveyResponseService.updateResponse(
                response,
                new ResponsibleEntity(bundle.participantUser()),
                null,
                bundle.portalParticipantUser(),
                bundle.enrollee(),
                task.getId(),
                portal.getId());
    }
}
