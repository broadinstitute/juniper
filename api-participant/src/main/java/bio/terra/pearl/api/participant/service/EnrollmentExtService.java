package bio.terra.pearl.api.participant.service;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.workflow.EnrollmentService;
import bio.terra.pearl.core.service.workflow.RegistrationService;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EnrollmentExtService {
  private final AuthUtilService authUtilService;
  private final EnrollmentService enrollmentService;
  private final PortalEnvironmentService portalEnvironmentService;
  private final RegistrationService registrationService;
  private final StudyEnvironmentService studyEnvironmentService;
  private final EnrolleeService enrolleeService;
  private final ParticipantUserService participantUserService;

  public EnrollmentExtService(
      AuthUtilService authUtilService,
      EnrollmentService enrollmentService,
      PortalEnvironmentService portalEnvironmentService,
      RegistrationService registrationService,
      StudyEnvironmentService studyEnvironmentService,
      EnrolleeService enrolleeService,
      ParticipantUserService participantUserService) {
    this.authUtilService = authUtilService;
    this.enrollmentService = enrollmentService;
    this.portalEnvironmentService = portalEnvironmentService;
    this.registrationService = registrationService;
    this.studyEnvironmentService = studyEnvironmentService;
    this.enrolleeService = enrolleeService;
    this.participantUserService = participantUserService;
  }

  /**
   * Enroll a governed user into a study. If the governed user is new, they will be registered and
   * enrolled. If the governed user already exists, they will simply be enrolled.
   *
   * @param operator Proxy user
   * @param portalShortcode Portal shortcode
   * @param envName Environment name
   * @param studyShortcode Study shortcode
   * @param preEnrollmentId Pre-enrollment response ID
   * @param governedPpUserId Governed pp user ID, or null if they need a user registered
   * @return New enrollee
   */
  public HubResponse enrollGovernedUser(
      ParticipantUser operator,
      String portalShortcode,
      EnvironmentName envName,
      String studyShortcode,
      UUID preEnrollmentId,
      UUID governedPpUserId // could be null if a totally new user
      ) {

    PortalParticipantUser portalParticipantUser =
        authUtilService
            .authParticipantToPortal(operator.getId(), portalShortcode, envName)
            .ppUser();

    Enrollee proxy =
        fetchOrCreateProxyEnrollee(operator, portalParticipantUser, studyShortcode, envName);

    if (governedPpUserId == null) {
      String governedUserName =
          registrationService.getGovernedUsername(
              operator.getUsername(), operator.getEnvironmentName());

      return enrollmentService.registerAndEnrollGovernedUser(
          envName,
          studyShortcode,
          proxy,
          operator,
          portalParticipantUser,
          preEnrollmentId,
          governedUserName);
    } else {
      PortalParticipantUser governedPpUser =
          authUtilService.authParticipantUserToPortalParticipantUser(
              operator.getId(), governedPpUserId);

      ParticipantUser governedUser =
          participantUserService
              .find(governedPpUser.getParticipantUserId())
              .orElseThrow(IllegalStateException::new);

      return enrollmentService.enrollGovernedUser(
          envName,
          studyShortcode,
          proxy,
          operator,
          portalParticipantUser,
          governedUser,
          governedPpUser,
          preEnrollmentId);
    }
  }

  private Enrollee fetchOrCreateProxyEnrollee(
      ParticipantUser user,
      PortalParticipantUser ppUser,
      String studyShortcode,
      EnvironmentName envName) {
    return enrolleeService
        .findByParticipantUserIdAndStudyEnv(ppUser.getParticipantUserId(), studyShortcode, envName)
        .orElseGet(
            () ->
                enrollmentService
                    .enroll(ppUser, envName, studyShortcode, user, ppUser, null, false)
                    .getResponse());
  }
}
