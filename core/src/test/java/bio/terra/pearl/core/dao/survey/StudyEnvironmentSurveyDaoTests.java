package bio.terra.pearl.core.dao.survey;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.study.StudyEnvironmentSurveyDao;
import bio.terra.pearl.core.factory.StudyEnvironmentBundle;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

public class StudyEnvironmentSurveyDaoTests extends BaseSpringBootTest {
    @Autowired
    private SurveyFactory surveyFactory;
    @Autowired
    private StudyEnvironmentSurveyDao studyEnvironmentSurveyDao;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;

    @Test
    @Transactional
    public void testFindSurveyNoContent(TestInfo testInfo) {
        StudyEnvironmentBundle bundle = studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);

        Survey survey = surveyFactory.buildPersisted(getTestName(testInfo));
        surveyFactory.attachToEnv(survey, bundle.getStudyEnv().getId(), true);

        // test fetching with no content
        List<StudyEnvironmentSurvey> configuredSurveys = studyEnvironmentSurveyDao.findAllWithSurveyNoContent(List.of(bundle.getStudyEnv().getId()), null, null);
        assertThat(configuredSurveys, hasSize(1));
        assertThat(configuredSurveys.get(0).getSurvey().getContent(), nullValue());

        // no surveys are inactive
        configuredSurveys = studyEnvironmentSurveyDao.findAllWithSurveyNoContent(List.of(bundle.getStudyEnv().getId()), null, false);
        assertThat(configuredSurveys, hasSize(0));
    }
}
