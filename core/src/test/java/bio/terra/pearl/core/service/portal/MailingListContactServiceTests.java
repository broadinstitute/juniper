package bio.terra.pearl.core.service.portal;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.portal.MailingListContactFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.portal.MailingListContact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

public class MailingListContactServiceTests extends BaseSpringBootTest {
    @Autowired
    private PortalEnvironmentFactory portalEnvironmentFactory;
    @Autowired
    private MailingListContactService mailingListContactService;
    @Autowired
    private MailingListContactFactory mailingListContactFactory;

    @Test
    @Transactional
    public void testCrud(TestInfo info) {
        var portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
        var portalEnv2 = portalEnvironmentFactory.buildPersisted(getTestName(info));

        MailingListContact contact = mailingListContactFactory.builder(getTestName(info))
                .portalEnvironmentId(portalEnv.getId()).build();
        var savedContact = mailingListContactService.create(contact);
        DaoTestUtils.assertGeneratedProperties(savedContact);

        var foundContacts = mailingListContactService.findByPortalEnv(portalEnv.getId());
        assertThat(foundContacts, contains(savedContact));
        var otherContacts = mailingListContactService.findByPortalEnv(portalEnv2.getId());
        assertThat(otherContacts, hasSize(0));
    }
}
