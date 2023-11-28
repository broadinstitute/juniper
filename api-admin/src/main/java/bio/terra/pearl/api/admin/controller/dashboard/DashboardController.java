package bio.terra.pearl.api.admin.controller.dashboard;

import bio.terra.pearl.api.admin.api.DashboardApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.dashboard.DashboardExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.ParticipantDashboardAlert;
import bio.terra.pearl.core.model.admin.AdminUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class DashboardController implements DashboardApi {

  private HttpServletRequest request;
  private AuthUtilService authUtilService;
  private DashboardExtService dashboardExtService;
  private ObjectMapper objectMapper;

  public DashboardController(
      HttpServletRequest request,
      AuthUtilService authUtilService,
      DashboardExtService dashboardExtService,
      ObjectMapper objectMapper) {
    this.request = request;
    this.authUtilService = authUtilService;
    this.dashboardExtService = dashboardExtService;
    this.objectMapper = objectMapper;
  }

  @Override
  public ResponseEntity<Object> listPortalEnvAlerts(String portalShortcode, String envName) {
    AdminUser user = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);

    List<ParticipantDashboardAlert> alerts =
        dashboardExtService.listPortalEnvAlerts(portalShortcode, environmentName, user);

    return ResponseEntity.ok(alerts);
  }

  @Override
  public ResponseEntity<Object> updatePortalEnvAlert(
      String portalShortcode, String envName, String triggerName, Object body) {
    AdminUser user = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);

    ParticipantDashboardAlert alert =
        objectMapper.convertValue(body, ParticipantDashboardAlert.class);

    ParticipantDashboardAlert updatedAlert =
        dashboardExtService.updatePortalEnvAlert(
            portalShortcode, environmentName, triggerName, alert, user);

    return ResponseEntity.ok(updatedAlert);
  }

  @Override
  public ResponseEntity<Object> createPortalEnvAlert(
      String portalShortcode, String envName, String triggerName, Object body) {
    AdminUser user = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);

    ParticipantDashboardAlert alert =
        objectMapper.convertValue(body, ParticipantDashboardAlert.class);

    ParticipantDashboardAlert createdAlert =
        dashboardExtService.createPortalEnvAlert(
            portalShortcode, environmentName, triggerName, alert, user);

    return ResponseEntity.ok(createdAlert);
  }
}
