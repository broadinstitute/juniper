package bio.terra.pearl.core.factory.survey;

import bio.terra.pearl.core.model.survey.AnswerMapping;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.survey.SurveyService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class SurveyFactory {
    @Autowired
    private SurveyService surveyService;
    @Autowired
    private StudyEnvironmentSurveyService studyEnvironmentSurveyService;

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

    public StudyEnvironmentSurvey attachToEnv(Survey survey, UUID studyEnvironmentId, boolean active) {
        return studyEnvironmentSurveyService.create(StudyEnvironmentSurvey.builder()
                .surveyId(survey.getId())
                .active(active)
                .studyEnvironmentId(studyEnvironmentId)
                .build());
    }
}
