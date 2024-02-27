package bio.terra.pearl.api.participant.controller.participant;

import bio.terra.pearl.api.participant.api.ProfileApi;
import bio.terra.pearl.api.participant.service.ProfileExtService;
import bio.terra.pearl.api.participant.service.RequestUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
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
  public ResponseEntity<Object> updateProfile(
      String portalShortcode, String envName, UUID ppUserId, Object body) {
    ParticipantUser participantUser = requestUtilService.requireUser(request);
    Profile profile = objectMapper.convertValue(body, Profile.class);

    Profile updatedProfile =
        profileExtService.updateWithMailingAddress(
            portalShortcode,
            EnvironmentName.valueOfCaseInsensitive(envName),
            participantUser,
            ppUserId,
            profile);

    return ResponseEntity.ok(updatedProfile);
  }

  @Override
  public ResponseEntity<Object> findProfile(String portalShortcode, String envName, UUID ppUserId) {

    ParticipantUser participantUser = requestUtilService.requireUser(request);

    Profile found =
        profileExtService.findProfile(
            portalShortcode,
            EnvironmentName.valueOfCaseInsensitive(envName),
            participantUser,
            ppUserId);

    return ResponseEntity.ok(found);
  }
}
