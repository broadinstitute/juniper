package bio.terra.pearl.core.service.consent;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.consent.ConsentFormFactory;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.consent.ConsentForm;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ConsentFormServiceTests extends BaseSpringBootTest {
    @Autowired
    private ConsentFormFactory consentFormFactory;
    @Autowired
    private AdminUserFactory adminUserFactory;
    @Autowired
    private ConsentFormService consentFormService;
    @Test
    @Transactional
    public void testConsentCreate(TestInfo info) {
        ConsentForm consentForm = consentFormFactory.builder(getTestName(info)).build();
        ConsentForm savedForm = consentFormService.create(consentForm);
        Assertions.assertNotNull(savedForm.getId());
        Assertions.assertEquals(savedForm.getName(), consentForm.getName());
        Assertions.assertNotNull(savedForm.getCreatedAt());

        ConsentForm fetchedForm = consentFormService.findByStableId(savedForm.getStableId(), savedForm.getVersion()).get();
        Assertions.assertEquals(fetchedForm.getId(), savedForm.getId());
    }

    @Test
    @Transactional
    public void testCreateNewVersion(TestInfo info) {
        ConsentForm form = consentFormFactory.buildPersisted(getTestName(info));
        AdminUser user = adminUserFactory.buildPersisted(getTestName(info));
        String oldContent = form.getContent();
        String newContent = "totally different " + RandomStringUtils.randomAlphabetic(6);
        form.setContent(newContent);
        ConsentForm newSurvey = consentFormService.createNewVersion(form.getPortalId(), form);

        Assertions.assertNotEquals(newSurvey.getId(), form.getId());
        // check version was incremented and content was modified
        Assertions.assertEquals(form.getVersion() + 1, newSurvey.getVersion());
        Assertions.assertEquals(newContent, newSurvey.getContent());

        // confirm the existing survey wasn't modified
        ConsentForm fetchedOriginal = consentFormService.find(form.getId()).get();
        Assertions.assertEquals(oldContent, fetchedOriginal.getContent());
    }

    @Test
    @Transactional
    public void testAssignPublishedVersion(TestInfo info) {
        ConsentForm form = consentFormFactory.buildPersisted(getTestName(info));
        consentFormService.assignPublishedVersion(form.getId());
        form = consentFormService.find(form.getId()).get();
        assertThat(form.getPublishedVersion(), equalTo(1));

        String newContent = String.format("{\"pages\":[],\"title\":\"%s\"}", RandomStringUtils.randomAlphabetic(6));
        form.setContent(newContent);
        ConsentForm  newForm = consentFormService.createNewVersion(form.getPortalId(), form);

        Assertions.assertNotEquals(newForm.getId(), form.getId());
        // check published version was NOT copied
        assertThat(newForm.getPublishedVersion(), equalTo(null));

        consentFormService.assignPublishedVersion(newForm.getId());
        newForm = consentFormService.find(newForm.getId()).get();
        assertThat(newForm.getPublishedVersion(), equalTo(2));
    }

}
