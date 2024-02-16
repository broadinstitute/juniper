package bio.terra.pearl.api.participant.service;

import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.participant.ProfileService;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProfileExtService {
  private final AuthUtilService authUtilService;
  private final RequestUtilService requestUtilService;
  private final ProfileService profileService;
  private final PortalParticipantUserService portalParticipantUserService;

  public ProfileExtService(
      AuthUtilService authUtilService,
      RequestUtilService requestUtilService,
      ProfileService profileService,
      PortalParticipantUserService portalParticipantUserService) {
    this.authUtilService = authUtilService;
    this.requestUtilService = requestUtilService;
    this.profileService = profileService;
    this.portalParticipantUserService = portalParticipantUserService;
  }

  public Profile updateWithMailingAddress(
      String portalShortcode,
      String studyShortcode,
      String envName,
      ParticipantUser participantUser,
      String enrolleeShortcode,
      Profile profile) {
    Optional<PortalParticipantUser> portalParticipantUser =
        portalParticipantUserService.findOne(participantUser.getId(), portalShortcode);
    if (portalParticipantUser.isEmpty()) {
      throw new IllegalArgumentException("Unknown portal");
    }

    //    StudyEnvironment studyEnv = requestUtilService.getStudyEnv(studyShortcode, envName);

    Enrollee enrollee =
        authUtilService.authParticipantUserToEnrollee(participantUser.getId(), enrolleeShortcode);

    //    if (!studyEnv.getId().equals(enrollee.getStudyEnvironmentId())) {
    //      throw new IllegalArgumentException("Unknown study environment");
    //    }

    profile.setId(enrollee.getProfileId());

    return profileService.updateWithMailingAddress(
        profile,
        DataAuditInfo.builder()
            .enrolleeId(enrollee.getId())
            .portalParticipantUserId(portalParticipantUser.get().getId())
            .build());
  }
}
