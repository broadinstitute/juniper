package bio.terra.pearl.core.service.portal;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.DaoTestUtils;
import bio.terra.pearl.core.factory.portal.MailingListContactFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.portal.MailingListContact;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class MailingListContactServiceTests extends BaseSpringBootTest {
    @Autowired
    private PortalEnvironmentFactory portalEnvironmentFactory;
    @Autowired
    private MailingListContactService mailingListContactService;
    @Autowired
    private MailingListContactFactory mailingListContactFactory;

    @Test
    public void testCrud() {
        var portalEnv = portalEnvironmentFactory.buildPersisted("testMailingListCrud");
        var portalEnv2 = portalEnvironmentFactory.buildPersisted("testMailingListCrud");

        MailingListContact contact = mailingListContactFactory.builder("testMailingListCrud")
                .portalEnvironmentId(portalEnv.getId()).build();
        var savedContact = mailingListContactService.create(contact);
        DaoTestUtils.assertGeneratedProperties(savedContact);

        var foundContacts = mailingListContactService.findByPortalEnv(portalEnv.getId());
        assertThat(foundContacts, contains(savedContact));
        var otherContacts = mailingListContactService.findByPortalEnv(portalEnv2.getId());
        assertThat(otherContacts, hasSize(0));
    }
}
