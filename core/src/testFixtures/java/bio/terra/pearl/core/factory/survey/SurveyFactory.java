package bio.terra.pearl.core.factory.survey;

import bio.terra.pearl.core.model.survey.AnswerMapping;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.survey.SurveyService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SurveyFactory {
    @Autowired
    private SurveyService surveyService;

    public Survey.SurveyBuilder builder(String testName) {
        String randString = RandomStringUtils.randomAlphabetic(3);
        return Survey.builder().version(1)
                .stableId(testName + "_" + randString)
                .content("{\"pages\":[]}")
                .name("Name " + randString + " survey");
    }

    public Survey.SurveyBuilder builderWithDependencies(String testName) {
        return builder(testName);
    }

    public Survey buildPersisted(String testName) {
        return surveyService.create(builderWithDependencies(testName).build());
    }

    public Survey buildPersisted(String testName, List<AnswerMapping> mappings) {
        Survey survey = builderWithDependencies(testName)
                .answerMappings(mappings)
                .build();
        return surveyService.create(survey);
    }
}
