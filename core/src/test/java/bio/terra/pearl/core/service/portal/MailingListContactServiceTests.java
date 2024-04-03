package bio.terra.pearl.core.service.portal;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.portal.MailingListContactFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.portal.MailingListContact;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
        PortalEnvironment portalEnv2 = portalEnvironmentFactory.buildPersisted(getTestName(info));

        MailingListContact contact = mailingListContactFactory.builder(getTestName(info))
                .portalEnvironmentId(portalEnv.getId()).build();
        DataAuditInfo auditInfo = DataAuditInfo.builder().build();
        MailingListContact savedContact = mailingListContactService.create(contact, auditInfo);
        DaoTestUtils.assertGeneratedProperties(savedContact);

        List<MailingListContact> foundContacts = mailingListContactService.findByPortalEnv(portalEnv.getId());
        assertThat(foundContacts, contains(savedContact));
        List<MailingListContact> otherContacts = mailingListContactService.findByPortalEnv(portalEnv2.getId());
        assertThat(otherContacts, hasSize(0));
    }
}
