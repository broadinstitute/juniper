package bio.terra.pearl.api.participant.controller.enrollment;

import bio.terra.pearl.api.participant.api.EnrollmentApi;
import bio.terra.pearl.api.participant.service.AuthUtilService;
import bio.terra.pearl.api.participant.service.EnrollmentExtService;
import bio.terra.pearl.api.participant.service.RequestUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.service.portal.PortalWithPortalUser;
import bio.terra.pearl.core.service.workflow.EnrollmentService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class EnrollmentController implements EnrollmentApi {
  private final EnrollmentService enrollmentService;
  private final RequestUtilService requestUtilService;
  private final HttpServletRequest request;
  private final AuthUtilService authUtilService;
  private final EnrollmentExtService enrollmentExtService;

  public EnrollmentController(
      EnrollmentService enrollmentService,
      RequestUtilService requestUtilService,
      HttpServletRequest request,
      AuthUtilService authUtilService,
      EnrollmentExtService enrollmentExtService) {
    this.enrollmentService = enrollmentService;
    this.requestUtilService = requestUtilService;
    this.request = request;
    this.authUtilService = authUtilService;
    this.enrollmentExtService = enrollmentExtService;
  }

  @Override
  public ResponseEntity<Object> createEnrollee(
      String portalShortcode, String envName, String studyShortcode, UUID preEnrollResponseId) {
    ParticipantUser user = requestUtilService.requireUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    PortalWithPortalUser portalWithPortalUser =
        authUtilService.authParticipantToPortal(user.getId(), portalShortcode, environmentName);
    HubResponse hubResponse =
        enrollmentService.enroll(
            environmentName,
            studyShortcode,
            user,
            portalWithPortalUser.ppUser(),
            preEnrollResponseId);

    return ResponseEntity.ok(hubResponse);
  }

  @Override
  public ResponseEntity<Object> createGovernedUser(
      String portalShortcode,
      String envName,
      String studyShortcode,
      UUID preEnrollResponseId,
      UUID governedPpUserId) {
    ParticipantUser user = requestUtilService.requireUser(request);

    HubResponse response =
        enrollmentExtService.enrollGovernedUser(
            user,
            portalShortcode,
            EnvironmentName.valueOfCaseInsensitive(envName),
            studyShortcode,
            preEnrollResponseId,
            governedPpUserId);

    return ResponseEntity.ok(response);
  }
}
