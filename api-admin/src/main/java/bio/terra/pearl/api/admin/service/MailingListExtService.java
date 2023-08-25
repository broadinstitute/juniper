package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.MailingListContact;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.workflow.DataChangeRecord;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.portal.MailingListContactService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.workflow.DataChangeRecordService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MailingListExtService {
  private AuthUtilService authUtilService;
  private MailingListContactService mailingListContactService;
  private PortalEnvironmentService portalEnvironmentService;
  private DataChangeRecordService dataChangeRecordService;
  private ObjectMapper objectMapper;

  public MailingListExtService(
      AuthUtilService requestUtilService,
      MailingListContactService mailingListContactService,
      PortalEnvironmentService portalEnvironmentService,
      DataChangeRecordService dataChangeRecordService,
      ObjectMapper objectMapper) {
    this.authUtilService = requestUtilService;
    this.mailingListContactService = mailingListContactService;
    this.portalEnvironmentService = portalEnvironmentService;
    this.dataChangeRecordService = dataChangeRecordService;
    this.objectMapper = objectMapper;
  }

  public List<MailingListContact> getAll(
      String portalShortcode, EnvironmentName envName, AdminUser user) {
    Portal portal = authUtilService.authUserToPortal(user, portalShortcode);
    PortalEnvironment portalEnv =
        portalEnvironmentService.findOne(portal.getShortcode(), envName).get();
    return mailingListContactService.findByPortalEnv(portalEnv.getId());
  }

  @Transactional
  public void delete(
      String portalShortcode, EnvironmentName envName, UUID contactId, AdminUser user) {
    Portal portal = authUtilService.authUserToPortal(user, portalShortcode);
    PortalEnvironment portalEnv =
        portalEnvironmentService.findOne(portal.getShortcode(), envName).get();
    MailingListContact contact = mailingListContactService.find(contactId).get();
    if (!contact.getPortalEnvironmentId().equals(portalEnv.getId())) {
      throw new PermissionDeniedException("Contact does not belong to the given portal");
    }
    try {
      DataChangeRecord changeRecord =
          DataChangeRecord.builder()
              .modelName(MailingListContact.class.getSimpleName())
              .responsibleAdminUserId(user.getId())
              .portalEnvironmentId(portalEnv.getId())
              .newValue(null)
              .oldValue(objectMapper.writeValueAsString(contact))
              .build();
      dataChangeRecordService.create(changeRecord);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("could not create audit trail", e);
    }
    mailingListContactService.delete(contactId, CascadeProperty.EMPTY_SET);
  }
}
