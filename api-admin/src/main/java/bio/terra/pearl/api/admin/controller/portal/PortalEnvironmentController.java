package bio.terra.pearl.api.admin.controller.portal;

import bio.terra.pearl.api.admin.api.PortalEnvironmentApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.publishing.PortalPublishingService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class PortalEnvironmentController implements PortalEnvironmentApi {
  private HttpServletRequest request;
  private AuthUtilService authUtilService;
  private PortalPublishingService portalPublishingService;

  public PortalEnvironmentController(
      HttpServletRequest request,
      AuthUtilService authUtilService,
      PortalPublishingService portalPublishingService) {
    this.request = request;
    this.authUtilService = authUtilService;
    this.portalPublishingService = portalPublishingService;
  }

  @Override
  public ResponseEntity<Object> diff(String portalShortcode, String destEnv, String sourceEnv) {
    AdminUser user = authUtilService.requireAdminUser(request);
    authUtilService.authUserToPortal(user, portalShortcode);
    try {
      var changeRecord =
          portalPublishingService.diff(
              portalShortcode,
              EnvironmentName.valueOfCaseInsensitive(sourceEnv),
              EnvironmentName.valueOfCaseInsensitive(destEnv));
      return ResponseEntity.ok(changeRecord);
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }
}
