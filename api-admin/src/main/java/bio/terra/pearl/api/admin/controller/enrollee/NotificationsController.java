package bio.terra.pearl.api.admin.controller.enrollee;

import bio.terra.pearl.api.admin.api.NotificationsApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.notifications.NotificationExtService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.notification.Notification;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.service.notification.NotificationDispatcher;
import bio.terra.pearl.core.service.notification.NotificationService;
import bio.terra.pearl.core.service.notification.TriggerService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationsController implements NotificationsApi {
  private AuthUtilService authUtilService;
  private NotificationService notificationService;
  private TriggerService triggerService;
  private NotificationDispatcher notificationDispatcher;
  private NotificationExtService notificationExtService;
  private EnrolleeService enrolleeService;
  private ObjectMapper objectMapper;
  private HttpServletRequest request;

  public NotificationsController(
      AuthUtilService authUtilService,
      NotificationService notificationService,
      TriggerService triggerService,
      NotificationDispatcher notificationDispatcher,
      NotificationExtService notificationExtService,
      EnrolleeService enrolleeService,
      ObjectMapper objectMapper,
      HttpServletRequest request) {
    this.authUtilService = authUtilService;
    this.notificationService = notificationService;
    this.triggerService = triggerService;
    this.notificationDispatcher = notificationDispatcher;
    this.notificationExtService = notificationExtService;
    this.enrolleeService = enrolleeService;
    this.objectMapper = objectMapper;
    this.request = request;
  }

  @Override
  public ResponseEntity<Object> find(
      String portalShortcode, String studyShortcode, String envName, String enrolleeShortcode) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    authUtilService.authUserToStudy(adminUser, portalShortcode, studyShortcode);
    Enrollee enrollee = enrolleeService.findOneByShortcode(enrolleeShortcode).get();
    List<Notification> notifications = notificationService.findByEnrolleeId(enrollee.getId());
    return ResponseEntity.ok(notifications);
  }
}
