package bio.terra.pearl.api.participant.controller.enrollment;

import java.util.UUID;

import bio.terra.pearl.api.participant.api.EnrollmentApi;
import bio.terra.pearl.api.participant.service.AuthUtilService;
import bio.terra.pearl.api.participant.service.RequestUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.service.portal.PortalWithPortalUser;
import bio.terra.pearl.core.service.workflow.EnrollmentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class EnrollmentController implements EnrollmentApi {
  private EnrollmentService enrollmentService;
  private RequestUtilService requestUtilService;
  private HttpServletRequest request;
  private AuthUtilService authUtilService;

  public EnrollmentController(
      EnrollmentService enrollmentService,
      RequestUtilService requestUtilService,
      HttpServletRequest request,
      AuthUtilService authUtilService) {
    this.enrollmentService = enrollmentService;
    this.requestUtilService = requestUtilService;
    this.request = request;
    this.authUtilService = authUtilService;
  }

  @Override
  public ResponseEntity<Object> createEnrollee(
      String portalShortcode,
      String envName,
      String studyShortcode,
      UUID preEnrollResponseId,
      Boolean isProxy) {
    ParticipantUser user = requestUtilService.requireUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    PortalWithPortalUser portalWithPortalUser =
        authUtilService.authParticipantToPortal(user.getId(), portalShortcode, environmentName);
    HubResponse hubResponse;
    if (isProxy) {
      hubResponse =
          enrollmentService.enrollAsProxy(
              environmentName,
              studyShortcode,
              user,
              portalWithPortalUser.ppUser(),
              preEnrollResponseId);
    } else {
      hubResponse =
          enrollmentService.enroll(
              environmentName,
              studyShortcode,
              user,
              portalWithPortalUser.ppUser(),
              preEnrollResponseId,
              true);
    }

    return ResponseEntity.ok(hubResponse);
  }
}
