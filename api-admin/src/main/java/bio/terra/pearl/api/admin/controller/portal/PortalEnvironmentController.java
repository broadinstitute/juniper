package bio.terra.pearl.api.admin.controller.portal;

import bio.terra.pearl.api.admin.api.PortalEnvironmentApi;
import bio.terra.pearl.api.admin.service.RequestUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.publishing.PortalPublishingService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class PortalEnvironmentController implements PortalEnvironmentApi {
  private HttpServletRequest request;
  private RequestUtilService requestUtilService;
  private PortalPublishingService portalPublishingService;

  public PortalEnvironmentController(
      HttpServletRequest request,
      RequestUtilService requestUtilService,
      PortalPublishingService portalPublishingService) {
    this.request = request;
    this.requestUtilService = requestUtilService;
    this.portalPublishingService = portalPublishingService;
  }

  @Override
  public ResponseEntity<Object> diff(String portalShortcode, String sourceEnv, String destEnv) {
    AdminUser user = requestUtilService.getFromRequest(request);
    requestUtilService.authUserToPortal(user, portalShortcode);
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
