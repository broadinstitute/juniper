package bio.terra.pearl.api.participant.controller;

import bio.terra.pearl.api.participant.api.MailingListContactApi;
import bio.terra.pearl.api.participant.model.MailingListContactDto;
import bio.terra.pearl.api.participant.service.MailingListContactExtService;
import bio.terra.pearl.api.participant.service.RequestUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.portal.MailingListContact;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class MailingListContactController implements MailingListContactApi {
  private RequestUtilService requestUtilService;
  private MailingListContactExtService mailingListContactExtService;
  private HttpServletRequest request;

  public MailingListContactController(
      RequestUtilService requestUtilService,
      MailingListContactExtService mailingListContactExtService,
      HttpServletRequest request) {
    this.requestUtilService = requestUtilService;
    this.mailingListContactExtService = mailingListContactExtService;
    this.request = request;
  }

  @Override
  public ResponseEntity<Object> create(
      String portalShortcode, String environmentName, MailingListContactDto body) {
    Optional<ParticipantUser> participantUserOpt = requestUtilService.getUserFromRequest(request);
    EnvironmentName envName = EnvironmentName.valueOfCaseInsensitive(environmentName);
    // do a get or create to avoid leaking information about whether the user has already signed up
    MailingListContact contact =
        mailingListContactExtService.createOrGet(
            body.getEmail(), body.getName(), portalShortcode, envName, participantUserOpt);
    // convert to a DTO to avoid leaking when the contact was first created
    var dto = new MailingListContactDto();
    dto.setEmail(contact.getEmail());
    dto.setName(contact.getName());
    return ResponseEntity.ok(dto);
  }
}
