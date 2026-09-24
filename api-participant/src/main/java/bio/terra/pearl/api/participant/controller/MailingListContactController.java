package bio.terra.pearl.api.participant.controller;

import bio.terra.pearl.api.participant.api.MailingListContactApi;
import bio.terra.pearl.api.participant.model.MailingListContactDto;
import bio.terra.pearl.api.participant.service.MailingListContactExtService;
import bio.terra.pearl.api.participant.service.RequestUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
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
    mailingListContactExtService.createOrGet(
        body.getEmail(), body.getName(), portalShortcode, envName, participantUserOpt);
    // echo back what was submitted, never the stored contact, so this can't be used to look up
    // the name of whoever is already signed up with a given email
    MailingListContactDto dto = new MailingListContactDto();
    dto.setEmail(body.getEmail());
    dto.setName(body.getName());
    return ResponseEntity.ok(dto);
  }
}
