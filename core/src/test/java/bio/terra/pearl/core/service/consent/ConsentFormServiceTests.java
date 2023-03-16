package bio.terra.pearl.core.service.consent;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.AdminUserFactory;
import bio.terra.pearl.core.factory.consent.ConsentFormFactory;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.consent.ConsentForm;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class ConsentFormServiceTests extends BaseSpringBootTest {
    @Autowired
    private ConsentFormFactory consentFormFactory;
    @Autowired
    private AdminUserFactory adminUserFactory;
    @Autowired
    private ConsentFormService consentFormService;
    @Test
    @Transactional
    public void testConsentCreate() {
        ConsentForm consentForm = consentFormFactory.builder("testConsentCreate").build();
        ConsentForm savedForm = consentFormService.create(consentForm);
        Assertions.assertNotNull(savedForm.getId());
        Assertions.assertEquals(savedForm.getName(), consentForm.getName());
        Assertions.assertNotNull(savedForm.getCreatedAt());

        ConsentForm fetchedForm = consentFormService.findByStableId(savedForm.getStableId(), savedForm.getVersion()).get();
        Assertions.assertEquals(fetchedForm.getId(), savedForm.getId());
    }

    @Test
    @Transactional
    public void testCreateNewVersion() {
        ConsentForm survey = consentFormFactory.buildPersisted("testPublishConsent");
        AdminUser user = adminUserFactory.buildPersisted("testPublishConsent");
        String oldContent = survey.getContent();
        String newContent = "totally different " + RandomStringUtils.randomAlphabetic(6);
        survey.setContent(newContent);
        ConsentForm newSurvey = consentFormService.createNewVersion(user, survey.getPortalId(), survey);

        Assertions.assertNotEquals(newSurvey.getId(), survey.getId());
        // check version was incremented and content was modified
        Assertions.assertEquals(survey.getVersion() + 1, newSurvey.getVersion());
        Assertions.assertEquals(newContent, newSurvey.getContent());

        // confirm the existing survey wasn't modified
        ConsentForm fetchedOriginal = consentFormService.find(survey.getId()).get();
        Assertions.assertEquals(oldContent, fetchedOriginal.getContent());
    }
}
