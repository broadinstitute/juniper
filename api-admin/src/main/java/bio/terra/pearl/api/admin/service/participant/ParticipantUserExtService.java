package bio.terra.pearl.api.admin.service.participant;

import bio.terra.pearl.api.admin.service.auth.EnforcePortalPermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnvAuthContext;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ParticipantUserExtService {
  private final ParticipantUserService participantUserService;
  private final EnrolleeService enrolleeService;
  private final StudyEnvironmentService studyEnvironmentService;

  public ParticipantUserExtService(
      ParticipantUserService participantUserService,
      EnrolleeService enrolleeService,
      StudyEnvironmentService studyEnvironmentService) {
    this.participantUserService = participantUserService;
    this.enrolleeService = enrolleeService;
    this.studyEnvironmentService = studyEnvironmentService;
  }

  @EnforcePortalPermission(permission = "participant_data_view")
  public ParticipantUsersWithEnrollees list(PortalEnvAuthContext authContext) {
    List<ParticipantUser> participantUsers =
        participantUserService.findAllByPortalEnv(
            authContext.getPortal().getId(), authContext.getEnvironmentName());
    List<Enrollee> enrollees =
        enrolleeService.findAllByPortalEnv(
            authContext.getPortal().getId(), authContext.getEnvironmentName());
    enrolleeService.attachProfiles(enrollees);
    return new ParticipantUsersWithEnrollees(participantUsers, enrollees);
  }
}
