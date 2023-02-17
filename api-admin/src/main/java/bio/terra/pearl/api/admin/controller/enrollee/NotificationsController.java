package bio.terra.pearl.api.admin.controller.enrollee;

import bio.terra.pearl.api.admin.api.NotificationsApi;
import bio.terra.pearl.api.admin.service.RequestUtilService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.notification.Notification;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.service.notification.NotificationService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationsController implements NotificationsApi {
  private RequestUtilService requestUtilService;
  private NotificationService notificationService;
  private EnrolleeService enrolleeService;
  private HttpServletRequest request;

  public NotificationsController(
      RequestUtilService requestUtilService,
      NotificationService notificationService,
      EnrolleeService enrolleeService,
      HttpServletRequest request) {
    this.requestUtilService = requestUtilService;
    this.notificationService = notificationService;
    this.enrolleeService = enrolleeService;
    this.request = request;
  }

  @Override
  public ResponseEntity<Object> find(
      String portalShortcode, String studyShortcode, String envName, String enrolleeShortcode) {
    AdminUser adminUser = requestUtilService.getFromRequest(request);
    requestUtilService.authUserToStudy(adminUser, portalShortcode, studyShortcode);
    Enrollee enrollee = enrolleeService.findOneByShortcode(enrolleeShortcode).get();
    List<Notification> notifications = notificationService.findByEnrolleeId(enrollee.getId());
    return ResponseEntity.ok(notifications);
  }
}
