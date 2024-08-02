package bio.terra.pearl.core.factory.survey;

import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.survey.AnswerMapping;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyType;
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
    @Autowired
    private PortalFactory portalFactory;

    public Survey.SurveyBuilder builder(String testName) {
        String randString = RandomStringUtils.randomAlphabetic(3);
        return Survey.builder().version(1)
                .stableId(testName + "_" + randString)
                .content("{\"pages\":[]}")
                .surveyType(SurveyType.RESEARCH)
                .name("Name " + randString + " survey");
    }

    public Survey.SurveyBuilder builderWithDependencies(String testName) {
        Portal portal = portalFactory.buildPersisted(testName);
        return builder(testName).portalId(portal.getId());
    }

    public Survey buildPersisted(String testName) {
        return surveyService.create(builderWithDependencies(testName).build());
    }

    public Survey buildPersisted(String testName, UUID portalId) {
        return surveyService.create(builder(testName).portalId(portalId).build());
    }

    public Survey buildPersisted(Survey.SurveyBuilder builder) {
        return surveyService.create(builder.build());
    }

    public Survey buildPersisted(String testName, List<AnswerMapping> mappings) {
        Survey survey = builderWithDependencies(testName)
                .answerMappings(mappings)
                .build();
        return surveyService.create(survey);
    }

    public StudyEnvironmentSurvey attachToEnv(Survey survey, UUID studyEnvironmentId, boolean active) {
        return attachToEnv(survey, studyEnvironmentId, active, 0);
    }

    public StudyEnvironmentSurvey attachToEnv(Survey survey, UUID studyEnvironmentId, boolean active, int surveyOrder) {
        return studyEnvironmentSurveyService.create(StudyEnvironmentSurvey.builder()
                .surveyId(survey.getId())
                .active(active)
                .surveyOrder(surveyOrder)
                .studyEnvironmentId(studyEnvironmentId)
                .build());
    }
}
