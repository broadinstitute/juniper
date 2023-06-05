package bio.terra.pearl.api.admin.service.notifications;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.samePropertyValuesAs;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.core.factory.notification.NotificationConfigFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.notification.*;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.notification.NotificationService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class NotificationExtServiceTests extends BaseSpringBootTest {
  @Autowired private EnrolleeFactory enrolleeFactory;
  @Autowired private NotificationExtService notificationExtService;
  @Autowired private PortalService portalService;
  @Autowired private StudyService studyService;
  @Autowired private StudyEnvironmentService studyEnvironmentService;
  @Autowired private NotificationConfigFactory notificationConfigFactory;
  @Autowired private NotificationService notificationService;
  @Autowired private ObjectMapper objectMapper;

  @Test
  @Transactional
  public void testSendAdHocNotification() throws Exception {
    AdminUser user = AdminUser.builder().superuser(true).build();
    var enrolleeBundle = enrolleeFactory.buildWithPortalUser("testSendAdHocNotification");
    Portal portal = portalService.find(enrolleeBundle.portalId()).get();
    Study study =
        studyService
            .findByStudyEnvironmentId(enrolleeBundle.enrollee().getStudyEnvironmentId())
            .get();
    StudyEnvironment studyEnv =
        studyEnvironmentService.find(enrolleeBundle.enrollee().getStudyEnvironmentId()).get();
    EnvironmentName environmentName = studyEnv.getEnvironmentName();
    NotificationConfig config =
        notificationConfigFactory.buildPersisted(
            NotificationConfig.builder()
                .deliveryType(NotificationDeliveryType.EMAIL)
                .notificationType(NotificationType.AD_HOC),
            studyEnv.getId(),
            enrolleeBundle.portalParticipantUser().getPortalEnvironmentId());
    var customMessages = Map.of("adHocMessage", "hello!");
    notificationExtService.sendAdHoc(
        user,
        portal.getShortcode(),
        study.getShortcode(),
        environmentName,
        List.of(enrolleeBundle.enrollee().getShortcode()),
        customMessages,
        config.getId());

    var notifications = notificationService.findByEnrolleeId(enrolleeBundle.enrollee().getId());
    assertThat(notifications, hasSize(1));
    assertThat(
        notifications.get(0),
        samePropertyValuesAs(
            Notification.builder()
                .notificationConfigId(config.getId())
                .deliveryStatus(NotificationDeliveryStatus.SKIPPED)
                .customMessages(objectMapper.writeValueAsString(customMessages))
                .studyEnvironmentId(studyEnv.getId())
                .portalEnvironmentId(
                    enrolleeBundle.portalParticipantUser().getPortalEnvironmentId())
                .deliveryType(NotificationDeliveryType.EMAIL)
                .enrolleeId(enrolleeBundle.enrollee().getId())
                .participantUserId(enrolleeBundle.enrollee().getParticipantUserId())
                .build(),
            "id",
            "createdAt",
            "lastUpdatedAt"));
  }
}
