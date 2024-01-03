package bio.terra.pearl.api.admin.controller.enrollee;

import bio.terra.pearl.api.admin.api.NotificationsApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.notifications.NotificationExtService;
import bio.terra.pearl.api.admin.service.notifications.SendgridEventService;
import bio.terra.pearl.core.dao.participant.ProfileDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.notification.Notification;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.model.notification.NotificationDeliveryStatus;
import bio.terra.pearl.core.model.notification.SendgridEvent;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.service.notification.NotificationConfigService;
import bio.terra.pearl.core.service.notification.NotificationDispatcher;
import bio.terra.pearl.core.service.notification.NotificationService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationsController implements NotificationsApi {
  private AuthUtilService authUtilService;
  private NotificationService notificationService;
  private SendgridEventService sendgridEventService;
  private NotificationConfigService notificationConfigService;
  private NotificationDispatcher notificationDispatcher;
  private NotificationExtService notificationExtService;
  private ProfileDao profileDao; // todo
  private EnrolleeService enrolleeService;
  private ObjectMapper objectMapper;
  private HttpServletRequest request;

  public NotificationsController(
      AuthUtilService authUtilService,
      NotificationService notificationService,
      SendgridEventService sendgridEventService,
      NotificationConfigService notificationConfigService,
      NotificationDispatcher notificationDispatcher,
      NotificationExtService notificationExtService,
      ProfileDao profileDao,
      EnrolleeService enrolleeService,
      ObjectMapper objectMapper,
      HttpServletRequest request) {
    this.authUtilService = authUtilService;
    this.notificationService = notificationService;
    this.sendgridEventService = sendgridEventService;
    this.notificationConfigService = notificationConfigService;
    this.notificationDispatcher = notificationDispatcher;
    this.notificationExtService = notificationExtService;
    this.profileDao = profileDao;
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
    enrollee.setProfile(profileDao.loadWithMailingAddress(enrollee.getProfileId()).get());

    System.out.println("email: " + enrollee.getProfile().getContactEmail());

    // todo: just query all the sendgrid events for the enrollee and set the opened flag. might also
    // want
    // to partition by study?
    notifications.forEach(
        notification -> {
          if (notification.getSendgridBatchId() != null && enrollee.getProfile() != null) {
            Optional<SendgridEvent> sendgridEvent =
                sendgridEventService.findByMessageIdAndToEmail(
                    notification.getSendgridBatchId(), enrollee.getProfile().getContactEmail());

            if (sendgridEvent.isPresent()) {
              System.out.println("sendgrid event: " + sendgridEvent.get().getOpensCount());
              notification.setOpened(sendgridEvent.get().getOpensCount() > 0);
              if (sendgridEvent.get().getStatus().equalsIgnoreCase("not delivered")) {
                notification.setDeliveryStatus(NotificationDeliveryStatus.NOT_DELIVERED);
              }
            }
          }
        });

    return ResponseEntity.ok(notifications);
  }

  @Override
  public ResponseEntity<Object> test(
      String portalShortcode, String envName, UUID configId, Object body) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    authUtilService.authUserToPortal(adminUser, portalShortcode);
    EnrolleeRuleData enrolleeRuleData = objectMapper.convertValue(body, EnrolleeRuleData.class);
    NotificationConfig config = notificationConfigService.find(configId).get();
    try {
      notificationDispatcher.dispatchTestNotification(config, enrolleeRuleData);
      return ResponseEntity.ok(config);
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body(e);
    }
  }

  /** send a one-off notification. */
  @Override
  public ResponseEntity<Object> adHoc(
      String portalShortcode, String studyShortcode, String envName, Object body) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    AdHocNotification adHoc = objectMapper.convertValue(body, AdHocNotification.class);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    NotificationConfig configUsed =
        notificationExtService.sendAdHoc(
            adminUser,
            portalShortcode,
            studyShortcode,
            environmentName,
            adHoc.enrolleeShortcodes,
            adHoc.customMessages,
            adHoc.notificationConfigId);
    return ResponseEntity.ok(configUsed);
  }

  /** object for specifying an adhoc notification. */
  public record AdHocNotification(
      List<String> enrolleeShortcodes,
      UUID notificationConfigId,
      Map<String, String> customMessages) {}
}
