package bio.terra.pearl.api.participant.controller.participant;

import bio.terra.pearl.api.participant.api.ProfileApi;
import bio.terra.pearl.api.participant.service.ProfileExtService;
import bio.terra.pearl.api.participant.service.RequestUtilService;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class ProfileController implements ProfileApi {
  private RequestUtilService requestUtilService;
  private ProfileExtService profileExtService;
  private HttpServletRequest request;
  private ObjectMapper objectMapper;

  public ProfileController(
      RequestUtilService requestUtilService,
      ProfileExtService profileExtService,
      ObjectMapper objectMapper,
      HttpServletRequest request) {
    this.requestUtilService = requestUtilService;
    this.profileExtService = profileExtService;
    this.objectMapper = objectMapper;
    this.request = request;
  }

  @Override
  public ResponseEntity<Object> updateProfileForEnrollee(
      String portalShortcode,
      String studyShortcode,
      String envName,
      String enrolleeShortcode,
      Object body) {
    System.out.println("called");
    Optional<ParticipantUser> participantUserOpt = requestUtilService.getUserFromRequest(request);
    if (participantUserOpt.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    System.out.println("ok let's go");

    Profile profile = objectMapper.convertValue(body, Profile.class);

    Profile updatedProfile =
        profileExtService.updateWithMailingAddress(
            portalShortcode,
            studyShortcode,
            envName,
            participantUserOpt.get(),
            enrolleeShortcode,
            profile);

    return ResponseEntity.ok(updatedProfile);
  }
}
