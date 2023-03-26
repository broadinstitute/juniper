package bio.terra.pearl.api.participant.controller.enrollment;

import bio.terra.pearl.api.participant.api.EnrollmentApi;
import bio.terra.pearl.api.participant.service.RequestUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.service.portal.PortalWithPortalUser;
import bio.terra.pearl.core.service.workflow.EnrollmentService;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class EnrollmentController implements EnrollmentApi {
  private EnrollmentService enrollmentService;
  private RequestUtilService requestUtilService;
  private HttpServletRequest request;

  public EnrollmentController(
      EnrollmentService enrollmentService,
      RequestUtilService requestUtilService,
      HttpServletRequest request) {
    this.enrollmentService = enrollmentService;
    this.requestUtilService = requestUtilService;
    this.request = request;
  }

  @Override
  public ResponseEntity<Object> createEnrollee(
      String portalShortcode, String envName, String studyShortcode, UUID preEnrollResponseId) {
    ParticipantUser user = requestUtilService.requireUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    PortalWithPortalUser portalWithPortalUser =
        requestUtilService.authParticipantToPortal(user.getId(), portalShortcode, environmentName);
    HubResponse hubResponse =
        enrollmentService.enroll(
            user,
            portalWithPortalUser.ppUser(),
            environmentName,
            studyShortcode,
            preEnrollResponseId);

    return ResponseEntity.ok(hubResponse);
  }
}
