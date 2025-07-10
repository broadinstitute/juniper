package bio.terra.pearl.api.participant.service;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.workflow.EnrollmentService;
import bio.terra.pearl.core.service.workflow.EventService;
import bio.terra.pearl.core.service.workflow.RegistrationService;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExternalEventExtService {
  private final AuthUtilService authUtilService;
  private final EnrollmentService enrollmentService;
  private final PortalEnvironmentService portalEnvironmentService;
  private final RegistrationService registrationService;
  private final StudyEnvironmentService studyEnvironmentService;
  private final EnrolleeService enrolleeService;
  private final ParticipantUserService participantUserService;
  private final PortalParticipantUserService portalParticipantUserService;
  private final EventService eventService;

  public ExternalEventExtService(
      AuthUtilService authUtilService,
      EnrollmentService enrollmentService,
      PortalEnvironmentService portalEnvironmentService,
      RegistrationService registrationService,
      StudyEnvironmentService studyEnvironmentService,
      EnrolleeService enrolleeService,
      ParticipantUserService participantUserService,
      PortalParticipantUserService portalParticipantUserService,
      EventService eventService) {
    this.authUtilService = authUtilService;
    this.enrollmentService = enrollmentService;
    this.portalEnvironmentService = portalEnvironmentService;
    this.registrationService = registrationService;
    this.studyEnvironmentService = studyEnvironmentService;
    this.enrolleeService = enrolleeService;
    this.participantUserService = participantUserService;
    this.portalParticipantUserService = portalParticipantUserService;
    this.eventService = eventService;
  }

  /**
   * Track a failed login event asynchronously. Since events must be attached to an enrollee, only
   * tracks failed logins for registered users.
   */
  @Async
  public void trackFailedLoginEventAsync(
      String portalShortcode, EnvironmentName envName, String username) {
    try {
      trackFailedLogin(portalShortcode, envName, username);
    } catch (Exception e) {
      log.error(
          "Error tracking failed login for user {} in portal {} on environment {}: {}",
          username,
          portalShortcode,
          envName,
          e.getMessage(),
          e);
    }
  }

  public void trackFailedLogin(String portalShortcode, EnvironmentName envName, String username) {

    Optional<PortalParticipantUser> ppUserOpt =
        portalParticipantUserService.findOne(username, portalShortcode, envName);

    if (ppUserOpt.isEmpty()) {
      return; // user not in portal, nothing to track
    }
    PortalParticipantUser ppUser = ppUserOpt.get();

    List<Enrollee> enrollees = enrolleeService.findByPortalParticipantUser(ppUser);

    if (enrollees.isEmpty()) {
      return; // no enrollee to track
    }

    for (Enrollee enrollee : enrollees) {
      eventService.publishEnrolleeFailedLoginEvent(enrollee, ppUser);
    }
  }
}
