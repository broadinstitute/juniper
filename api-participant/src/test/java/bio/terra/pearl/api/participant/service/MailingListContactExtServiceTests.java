package bio.terra.pearl.api.participant.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import bio.terra.pearl.api.participant.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.service.portal.PortalService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class MailingListContactExtServiceTests extends BaseSpringBootTest {
  @Autowired MailingListContactExtService mailingListContactExtService;
  @Autowired PortalEnvironmentFactory portalEnvironmentFactory;
  @Autowired PortalService portalService;

  @Test
  @Transactional
  public void testGetOrCreate(TestInfo info) {
    PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
    String shortcode = portalService.find(portalEnv.getPortalId()).get().getShortcode();
    var contact =
        mailingListContactExtService.createOrGet(
            "test1@test.com",
            "fakename",
            shortcode,
            portalEnv.getEnvironmentName(),
            Optional.empty());
    DaoTestUtils.assertGeneratedProperties(contact);

    // check you can create again with same info
    var contact2 =
        mailingListContactExtService.createOrGet(
            "test1@test.com", "fakename", shortcode, portalEnv.getEnvironmentName(), null);
    DaoTestUtils.assertGeneratedProperties(contact2);
    assertThat(contact.getId(), equalTo(contact2.getId()));
  }
}
