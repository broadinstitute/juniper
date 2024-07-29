package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class StudyEnvironmentSurveyServiceTests extends BaseSpringBootTest {
    @Autowired
    private StudyEnvironmentSurveyService studyEnvironmentSurveyService;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    private SurveyFactory surveyFactory;
    @Autowired
    private SurveyService surveyService;

    @Test
    @Transactional
    public void testUniqueActiveLogic(TestInfo testInfo) {
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(getTestName(testInfo));
        Survey survey = surveyFactory.buildPersisted(getTestName(testInfo));

        surveyFactory.attachToEnv(survey, studyEnv.getId(), true);

        // try to attach a different version of the survey to the same environment
        Survey newSurvey = surveyService.createNewVersion(survey.getPortalId(), survey);

        final StudyEnvironmentSurvey studyEnvSurvey = StudyEnvironmentSurvey.builder()
                .studyEnvironmentId(studyEnv.getId())
                .surveyId(newSurvey.getId())
                .active(true)
                .surveyOrder(1)
                .build();
        Assertions.assertThrows(IllegalArgumentException.class, () -> studyEnvironmentSurveyService.create(studyEnvSurvey));

        // confirm that the new survey can be added as inactive
        studyEnvSurvey.setActive(false);
        final StudyEnvironmentSurvey savedStudyEnvSurvey = studyEnvironmentSurveyService.create(studyEnvSurvey);

        // but now we can't update it to active
        savedStudyEnvSurvey.setActive(true);
        Assertions.assertThrows(IllegalArgumentException.class, () -> studyEnvironmentSurveyService.update(savedStudyEnvSurvey));
    }

}
