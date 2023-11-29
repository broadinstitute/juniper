package bio.terra.pearl.api.admin.service.dashboard;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.ParticipantDashboardAlert;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.portal.PortalDashboardConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.exception.PortalEnvironmentMissing;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class DashboardExtService {

  private PortalDashboardConfigService portalDashboardConfigService;
  private PortalEnvironmentService portalEnvironmentService;
  private AuthUtilService authUtilService;

  public DashboardExtService(
      PortalDashboardConfigService portalDashboardConfigService,
      PortalEnvironmentService portalEnvironmentService,
      AuthUtilService authUtilService) {
    this.portalDashboardConfigService = portalDashboardConfigService;
    this.portalEnvironmentService = portalEnvironmentService;
    this.authUtilService = authUtilService;
  }

  public List<ParticipantDashboardAlert> listPortalEnvAlerts(
      String portalShortcode, EnvironmentName envName, AdminUser user) {
    Portal authedPortal = authUtilService.authUserToPortal(user, portalShortcode);

    PortalEnvironment portalEnv =
        portalEnvironmentService
            .findOne(authedPortal.getShortcode(), envName)
            .orElseThrow(PortalEnvironmentMissing::new);

    return portalDashboardConfigService.findByPortalEnvId(portalEnv.getId());
  }

  public ParticipantDashboardAlert updatePortalEnvAlert(
      String portalShortcode,
      EnvironmentName envName,
      String triggerName,
      ParticipantDashboardAlert newAlert,
      AdminUser user) {
    Portal authedPortal = authUtilService.authUserToPortal(user, portalShortcode);

    if (!envName.equals(EnvironmentName.sandbox)) {
      throw new IllegalArgumentException(
          "You can only update dashboard alerts in the sandbox environment");
    }

    if (!triggerName.equals(newAlert.getTrigger())) {
      throw new IllegalArgumentException(
          "Trigger name specified in payload does not match trigger name specified in path");
    }

    PortalEnvironment portalEnv =
        portalEnvironmentService
            .findOne(authedPortal.getShortcode(), envName)
            .orElseThrow(PortalEnvironmentMissing::new);

    ParticipantDashboardAlert alert =
        portalDashboardConfigService
            .findByPortalEnvIdAndTrigger(portalEnv.getId(), triggerName)
            .orElseThrow(() -> new NotFoundException("The specified alert does not exist"));

    alert.setTitle(newAlert.getTitle());
    alert.setDetail(newAlert.getDetail());
    return portalDashboardConfigService.update(alert);
  }

  public ParticipantDashboardAlert createPortalEnvAlert(
      String portalShortcode,
      EnvironmentName envName,
      String triggerName,
      ParticipantDashboardAlert newAlert,
      AdminUser user) {
    Portal authedPortal = authUtilService.authUserToPortal(user, portalShortcode);

    if (!envName.equals(EnvironmentName.sandbox)) {
      throw new IllegalArgumentException(
          "You can only create dashboard alerts in the sandbox environment");
    }

    if (!triggerName.equals(newAlert.getTrigger())) {
      throw new IllegalArgumentException(
          "Trigger name specified in payload does not match trigger name specified in path");
    }

    PortalEnvironment portalEnv =
        portalEnvironmentService
            .findOne(authedPortal.getShortcode(), envName)
            .orElseThrow(PortalEnvironmentMissing::new);

    newAlert.setPortalEnvironmentId(portalEnv.getId());
    newAlert.setStudyEnvironmentId(null);
    return portalDashboardConfigService.create(newAlert);
  }
}
