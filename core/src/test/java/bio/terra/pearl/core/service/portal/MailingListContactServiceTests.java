package bio.terra.pearl.core.service.portal;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.portal.MailingListContactFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.portal.MailingListContact;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

import bio.terra.pearl.core.model.portal.PortalEnvironment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class MailingListContactServiceTests extends BaseSpringBootTest {
    @Autowired
    private PortalEnvironmentFactory portalEnvironmentFactory;
    @Autowired
    private MailingListContactService mailingListContactService;
    @Autowired
    private MailingListContactFactory mailingListContactFactory;

    @Test
    @Transactional
    public void testCrud() {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted("testMailingListCrud");
        PortalEnvironment portalEnv2 = portalEnvironmentFactory.buildPersisted("testMailingListCrud");

        MailingListContact contact = mailingListContactFactory.builder("testMailingListCrud")
                .portalEnvironmentId(portalEnv.getId()).build();
        MailingListContact savedContact = mailingListContactService.create(contact);
        DaoTestUtils.assertGeneratedProperties(savedContact);

        List<MailingListContact> foundContacts = mailingListContactService.findByPortalEnv(portalEnv.getId());
        assertThat(foundContacts, contains(savedContact));
        List<MailingListContact> otherContacts = mailingListContactService.findByPortalEnv(portalEnv2.getId());
        assertThat(otherContacts, hasSize(0));
    }
}
