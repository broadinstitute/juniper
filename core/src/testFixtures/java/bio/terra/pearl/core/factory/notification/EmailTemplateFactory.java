package bio.terra.pearl.core.factory.notification;

import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.notification.email.EmailTemplateService;
import bio.terra.pearl.core.service.portal.PortalService;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailTemplateFactory {
    @Autowired
    private EmailTemplateService emailTemplateService;
    @Autowired
    private PortalFactory portalFactory;

    public EmailTemplate buildPersisted(String testname, UUID portalId) {
        EmailTemplate template = EmailTemplate.builder()
                .stableId(testname + RandomStringUtils.randomAlphabetic(4))
                .name("Template name")
                .version(1)
                .portalId(portalId).build();
        return emailTemplateService.create(template);
    }

    public EmailTemplate buildPersisted(String testname) {
        Portal portal = portalFactory.buildPersisted(testname);
        return buildPersisted(testname, portal.getId());
    }
}
