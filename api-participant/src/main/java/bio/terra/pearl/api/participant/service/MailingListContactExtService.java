package bio.terra.pearl.api.participant.service;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.portal.MailingListContact;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.service.portal.MailingListContactService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class MailingListContactExtService {
  private PortalEnvironmentService portalEnvironmentService;
  private StudyEnvironmentService studyEnvironmentService;
  private MailingListContactService mailingListContactService;

  public MailingListContactExtService(
      PortalEnvironmentService portalEnvironmentService,
      StudyEnvironmentService studyEnvironmentService,
      MailingListContactService mailingListContactService) {
    this.portalEnvironmentService = portalEnvironmentService;
    this.studyEnvironmentService = studyEnvironmentService;
    this.mailingListContactService = mailingListContactService;
  }

  public MailingListContact create(
      String email,
      String name,
      String portalShortcode,
      EnvironmentName envName,
      Optional<ParticipantUser> userOpt) {
    // mailing lists are open-access -- no need to auth anything.  The user is optional
    PortalEnvironment portalEnv = portalEnvironmentService.findOne(portalShortcode, envName).get();
    MailingListContact contact =
        MailingListContact.builder()
            .name(name)
            .email(email)
            .portalEnvironmentId(portalEnv.getId())
            .participantUserId(userOpt.isPresent() ? userOpt.get().getId() : null)
            .build();
    return mailingListContactService.create(contact);
  }
}
