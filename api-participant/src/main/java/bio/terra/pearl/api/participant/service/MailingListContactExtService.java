package bio.terra.pearl.api.participant.service;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.portal.MailingListContact;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.service.portal.MailingListContactService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class MailingListContactExtService {
  private final PortalEnvironmentService portalEnvironmentService;
  private final MailingListContactService mailingListContactService;

  public MailingListContactExtService(
      PortalEnvironmentService portalEnvironmentService,
      MailingListContactService mailingListContactService) {
    this.portalEnvironmentService = portalEnvironmentService;
    this.mailingListContactService = mailingListContactService;
  }

  public MailingListContact createOrGet(
      String email,
      String name,
      String portalShortcode,
      EnvironmentName envName,
      Optional<ParticipantUser> userOpt) {
    // mailing lists are open-access -- no need to auth anything.  The user is optional
    PortalEnvironment portalEnv = portalEnvironmentService.findOne(portalShortcode, envName).get();
    Optional<MailingListContact> existing =
        mailingListContactService.findByPortalEnv(portalEnv.getId(), email);
    if (existing.isPresent()) {
      return existing.get();
    }
    MailingListContact contact =
        MailingListContact.builder()
            .name(name)
            .email(email)
            .portalEnvironmentId(portalEnv.getId())
            .participantUserId(userOpt.isPresent() ? userOpt.get().getId() : null)
            .build();

    DataAuditInfo auditInfo = DataAuditInfo.builder().build();
    userOpt.ifPresentOrElse(
        user -> auditInfo.setResponsibleEntity(new ResponsibleEntity(user)),
        () -> auditInfo.setResponsibleEntity(new ResponsibleEntity(true)));

    return mailingListContactService.create(contact, auditInfo);
  }
}
