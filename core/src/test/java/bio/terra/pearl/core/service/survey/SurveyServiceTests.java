package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.AdminUserFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.survey.Survey;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class SurveyServiceTests extends BaseSpringBootTest {
    @Autowired
    private SurveyService surveyService;
    @Autowired
    private SurveyFactory surveyFactory;
    @Autowired
    private AdminUserFactory adminUserFactory;

    @Test
    @Transactional
    public void testCreateSurvey() {
        Survey survey = surveyFactory.builder("testPublishSurvey").build();
        Survey savedSurvey = surveyService.create(survey);
        Assertions.assertNotNull(savedSurvey.getId());
        Assertions.assertEquals(savedSurvey.getName(), survey.getName());
        Assertions.assertNotNull(savedSurvey.getCreatedAt());

        Survey fetchedSurvey = surveyService.findByStableId(savedSurvey.getStableId(), savedSurvey.getVersion()).get();
        Assertions.assertEquals(fetchedSurvey.getId(), savedSurvey.getId());
    }

    @Test
    @Transactional
    public void testCreateNewVersion() {
        Survey survey = surveyFactory.buildPersisted("testPublishSurvey");
        AdminUser user = adminUserFactory.buildPersisted("testPublishSurvey");
        String oldContent = survey.getContent();
        String newContent = "totally new " + RandomStringUtils.randomAlphabetic(6);
        survey.setContent(newContent);
        Survey newSurvey = surveyService.createNewVersion(user, survey.getPortalId(), survey);

        Assertions.assertNotEquals(newSurvey.getId(), survey.getId());
        // check version was incremented and content was modified
        Assertions.assertEquals(survey.getVersion() + 1, newSurvey.getVersion());
        Assertions.assertEquals(newContent, newSurvey.getContent());

        // confirm the existing survey wasn't modified
        Survey fetchedOriginal = surveyService.find(survey.getId()).get();
        Assertions.assertEquals(oldContent, fetchedOriginal.getContent());
    }
}
