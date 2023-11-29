package bio.terra.pearl.api.admin.service.dashboard;

import static bio.terra.pearl.core.model.dashboard.AlertTrigger.NO_ACTIVITIES_REMAIN;
import static org.mockito.Mockito.when;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.dashboard.ParticipantDashboardAlert;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.portal.PortalDashboardConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = DashboardExtService.class)
@WebMvcTest
public class DashboardExtServiceTests {

  @Autowired private DashboardExtService dashboardExtService;
  @MockBean private AuthUtilService mockAuthUtilService;
  @MockBean private PortalDashboardConfigService portalDashboardConfigService;
  @MockBean private PortalEnvironmentService portalEnvironmentService;

  @Test
  public void listPortalEnvAlertsRequiresAdmin() {
    AdminUser user = AdminUser.builder().superuser(false).build();
    when(mockAuthUtilService.authUserToPortal(user, "testPortal"))
        .thenThrow(new PermissionDeniedException("test"));
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () -> dashboardExtService.listPortalEnvAlerts("testPortal", EnvironmentName.sandbox, user));
  }

  @Test
  public void updatePortalEnvAlertRequiresAdmin() {
    AdminUser user = AdminUser.builder().superuser(false).build();
    ParticipantDashboardAlert alert = new ParticipantDashboardAlert();
    when(mockAuthUtilService.authUserToPortal(user, "testPortal"))
        .thenThrow(new PermissionDeniedException("test"));
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () ->
            dashboardExtService.updatePortalEnvAlert(
                "testPortal", EnvironmentName.sandbox, NO_ACTIVITIES_REMAIN, alert, user));
  }

  @Test
  public void updatePortalEnvAlertRequiresSandbox() {
    AdminUser user = AdminUser.builder().superuser(true).build();
    ParticipantDashboardAlert alert = new ParticipantDashboardAlert();
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () ->
            dashboardExtService.updatePortalEnvAlert(
                "testPortal", EnvironmentName.irb, NO_ACTIVITIES_REMAIN, alert, user));
  }

  @Test
  public void createPortalEnvAlertRequiresAdmin() {
    AdminUser user = AdminUser.builder().superuser(false).build();
    ParticipantDashboardAlert alert = new ParticipantDashboardAlert();
    when(mockAuthUtilService.authUserToPortal(user, "testPortal"))
        .thenThrow(new PermissionDeniedException("test"));
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () ->
            dashboardExtService.createPortalEnvAlert(
                "testPortal", EnvironmentName.sandbox, NO_ACTIVITIES_REMAIN, alert, user));
  }

  @Test
  public void createPortalEnvAlertRequiresSandbox() {
    AdminUser user = AdminUser.builder().superuser(true).build();
    ParticipantDashboardAlert alert = new ParticipantDashboardAlert();
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () ->
            dashboardExtService.createPortalEnvAlert(
                "testPortal", EnvironmentName.irb, NO_ACTIVITIES_REMAIN, alert, user));
  }
}
