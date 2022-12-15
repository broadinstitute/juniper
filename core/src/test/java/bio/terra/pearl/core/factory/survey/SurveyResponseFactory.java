package bio.terra.pearl.core.factory.survey;

import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SurveyResponseFactory {
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private SurveyFactory surveyFactory;

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
}
