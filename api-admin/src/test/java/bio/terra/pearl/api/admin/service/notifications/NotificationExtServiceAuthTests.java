package bio.terra.pearl.api.admin.service.notifications;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.notification.NotificationDispatcher;
import bio.terra.pearl.core.service.notification.TriggerService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.rule.EnrolleeContextService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@ContextConfiguration(classes = NotificationExtService.class)
@WebMvcTest
public class NotificationExtServiceAuthTests {
  @Autowired private MockMvc mockMvc;
  @Autowired private NotificationExtService notificationExtService;

  @MockBean private AuthUtilService mockAuthUtilService;
  @MockBean private EnrolleeService mockEnrolleeService;
  @MockBean private TriggerService mockTriggerService;
  @MockBean private NotificationDispatcher mockNotificationDispatcher;
  @MockBean
  private EnrolleeContextService mockEnrolleeContextService;
  @MockBean private StudyEnvironmentService mockStudyEnvironmentService;
  @MockBean private PortalEnvironmentService mockPortalEnvironmentService;
  @MockBean private StudyService mockStudyService;

  @Test
  public void sendAdHocAuthsToPortal() {
    AdminUser user = AdminUser.builder().superuser(false).build();
    when(mockAuthUtilService.authUserToPortal(user, "foo"))
        .thenThrow(new PermissionDeniedException("test1"));
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () ->
            notificationExtService.sendAdHoc(
                user,
                "foo",
                "studyCode",
                EnvironmentName.live,
                List.of("JOSALK"),
                null,
                UUID.randomUUID()));
  }

  @Test
  public void sendAdHocAuthsToStudy() {
    AdminUser user = AdminUser.builder().superuser(false).build();
    when(mockAuthUtilService.authUserToStudy(user, "foo", "studyCode"))
        .thenThrow(new PermissionDeniedException("test1"));
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () ->
            notificationExtService.sendAdHoc(
                user,
                "foo",
                "studyCode",
                EnvironmentName.live,
                List.of("JOSALK"),
                null,
                UUID.randomUUID()));
  }

  @Test
  public void sendAdHocChecksEnrolleesInStudy() {
    AdminUser user = AdminUser.builder().superuser(false).build();
    Enrollee enrollee = Enrollee.builder().shortcode("JOSALK").build();
    StudyEnvironment studyEnv = StudyEnvironment.builder().id(UUID.randomUUID()).build();
    List<String> shortcodes = List.of("JOSALK");
    when(mockEnrolleeService.findAllByShortcodes(shortcodes)).thenReturn(List.of(enrollee));
    when(mockStudyEnvironmentService.findByStudy("studyCode", EnvironmentName.live))
        .thenReturn(Optional.of(studyEnv));
    doThrow(new PermissionDeniedException(""))
        .when(mockAuthUtilService)
        .checkEnrolleeInStudyEnv(enrollee, studyEnv);
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () ->
            notificationExtService.sendAdHoc(
                user,
                "foo",
                "studyCode",
                EnvironmentName.live,
                List.of("JOSALK"),
                null,
                UUID.randomUUID()));
  }
}
