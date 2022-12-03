package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.survey.Survey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class SurveyServiceTests extends BaseSpringBootTest {
    @Autowired
    private SurveyService surveyService;
    @Autowired
    private SurveyFactory surveyFactory;

    @Test
    @Transactional
    public void testCreateSurvey() {
        Survey survey = surveyFactory.builder("testCreateStudy").build();
        Survey savedSurvey = surveyService.create(survey);
        Assertions.assertNotNull(savedSurvey.getId());
        Assertions.assertEquals(savedSurvey.getName(), survey.getName());
        Assertions.assertNotNull(savedSurvey.getCreatedAt());

        Survey fetchedSurvey = surveyService.findByStableId(savedSurvey.getStableId(), savedSurvey.getVersion()).get();
        Assertions.assertEquals(fetchedSurvey.getId(), savedSurvey.getId());
    }
}
