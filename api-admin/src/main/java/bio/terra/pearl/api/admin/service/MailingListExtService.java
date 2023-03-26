package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.MailingListContact;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.service.portal.MailingListContactService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MailingListExtService {
  private RequestUtilService requestUtilService;
  private MailingListContactService mailingListContactService;
  private PortalEnvironmentService portalEnvironmentService;

  public MailingListExtService(
      RequestUtilService requestUtilService,
      MailingListContactService mailingListContactService,
      PortalEnvironmentService portalEnvironmentService) {
    this.requestUtilService = requestUtilService;
    this.mailingListContactService = mailingListContactService;
    this.portalEnvironmentService = portalEnvironmentService;
  }

  public List<MailingListContact> getAll(
      String portalShortcode, EnvironmentName envName, AdminUser user) {
    Portal portal = requestUtilService.authUserToPortal(user, portalShortcode);
    PortalEnvironment portalEnv =
        portalEnvironmentService.findOne(portal.getShortcode(), envName).get();
    return mailingListContactService.findByPortalEnv(portalEnv.getId());
  }
}
