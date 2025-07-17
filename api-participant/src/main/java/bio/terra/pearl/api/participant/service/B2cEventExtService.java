package bio.terra.pearl.api.participant.service;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.workflow.EventService;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class B2cEventExtService {
  private final EnrolleeService enrolleeService;
  private final PortalParticipantUserService portalParticipantUserService;
  private final EventService eventService;

  public B2cEventExtService(
      EnrolleeService enrolleeService,
      PortalParticipantUserService portalParticipantUserService,
      EventService eventService) {
    this.enrolleeService = enrolleeService;
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
          "Error tracking failed login in portal {} on environment {}",
          portalShortcode,
          envName,
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
