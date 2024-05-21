package bio.terra.pearl.core.service.notification.email;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.notification.EmailTemplateFactory;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.portal.Portal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class EmailTemplateServiceTests extends BaseSpringBootTest {
    @Autowired
    private EmailTemplateFactory emailTemplateFactory;
    @Autowired
    private EmailTemplateService emailTemplateService;
    @Autowired
    private PortalFactory portalFactory;

    @Test
    @Transactional
    public void testAssignPublishedVersion(TestInfo info) {
        EmailTemplate form = emailTemplateFactory.buildPersisted(getTestName(info));
        assertThat(form.getPublishedVersion(), equalTo(null));
        emailTemplateService.assignPublishedVersion(form.getId());
        form = emailTemplateService.find(form.getId()).get();
        assertThat(form.getPublishedVersion(), equalTo(1));
    }

    @Test
    @Transactional
    public void testFindAdminEmails(TestInfo info) {
        EmailTemplate template = EmailTemplate.builder()
                .name("Admin Welcome Email")
                .stableId("admin_welcome_email")
                .version(1)
                .portalId(null).build();
        emailTemplateService.create(template);

        //Create another portal-specific template with the same stable id to confirm that we don't collide
        Portal portal = portalFactory.buildPersisted(getTestName(info));
        EmailTemplate portalTemplate = EmailTemplate.builder()
                .name("Portal Welcome Email")
                .stableId("admin_welcome_email")
                .version(1)
                .portalId(portal.getId()).build();
        emailTemplateService.create(portalTemplate);

        EmailTemplate adminTemplate = emailTemplateService.findAdminTemplateByStableId("admin_welcome_email", 1).get();
        assertThat(adminTemplate.getStableId(), equalTo("admin_welcome_email"));
        assertThat(adminTemplate.getVersion(), equalTo(1));
        assertThat(adminTemplate.getPortalId(), equalTo(null));
        assertThat(adminTemplate.getName(), equalTo("Admin Welcome Email"));
    }
}
