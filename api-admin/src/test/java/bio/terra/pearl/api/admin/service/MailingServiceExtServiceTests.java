package bio.terra.pearl.api.admin.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.MockAuthServiceAlwaysRejects;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.audit.DataChangeRecord;
import bio.terra.pearl.core.model.portal.MailingListContact;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.portal.MailingListContactService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.workflow.DataChangeRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class MailingServiceExtServiceTests extends BaseSpringBootTest {
  @Autowired MailingListExtService mailingListExtService;
  @Autowired MailingListContactService mailingListService;
  @Autowired PortalEnvironmentFactory portalEnvironmentFactory;
  @Autowired AdminUserFactory adminUserFactory;
  @Autowired PortalService portalService;
  @Autowired DataChangeRecordService dataChangeRecordService;
  @Autowired ObjectMapper objectMapper;

  @Test
  public void mailingListRequiresAuth() {
    MailingListExtService listExtService =
        new MailingListExtService(new MockAuthServiceAlwaysRejects(), null, null, null, null);
    // testing that this exception is thrown even when everything else is null is a good check that
    // no work is done prior to auth
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () ->
            listExtService.delete(
                "ourhealth", EnvironmentName.live, UUID.randomUUID(), new AdminUser()));
  }

  @Test
  @Transactional
  public void deleteMailingListContact(TestInfo info) throws Exception {
    PortalEnvironment portalEnvironment =
        portalEnvironmentFactory.buildPersisted(getTestName(info));
    AdminUser adminUser = adminUserFactory.buildPersisted(getTestName(info), true);
    Portal portal = portalService.find(portalEnvironment.getPortalId()).get();
    MailingListContact contact =
        mailingListService.create(
            MailingListContact.builder()
                .name("test1")
                .email("test1@test.com")
                .portalEnvironmentId(portalEnvironment.getId())
                .build());

    MailingListContact createdContact = mailingListService.find(contact.getId()).get();
    mailingListExtService.delete(
        portal.getShortcode(), portalEnvironment.getEnvironmentName(), contact.getId(), adminUser);
    assertThat(mailingListService.find(contact.getId()).isPresent(), equalTo(false));
    List<DataChangeRecord> changeRecords =
        dataChangeRecordService.findByPortalEnvironmentId(portalEnvironment.getId());
    assertThat(changeRecords, hasSize(1));
    assertThat(changeRecords.get(0).getResponsibleAdminUserId(), equalTo(adminUser.getId()));
    assertThat(
        changeRecords.get(0).getOldValue(),
        equalTo(objectMapper.writeValueAsString(createdContact)));
  }
}
