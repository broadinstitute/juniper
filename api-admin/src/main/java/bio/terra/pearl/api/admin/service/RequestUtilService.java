package bio.terra.pearl.api.admin.service;

import bio.terra.common.exception.UnauthorizedException;
import bio.terra.common.iam.BearerTokenFactory;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.portal.PortalService;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

/** Utility methods for handling requests */
@Service
public class RequestUtilService {
  private CurrentUserService currentUserService;
  private BearerTokenFactory bearerTokenFactory;
  private PortalService portalService;

  public RequestUtilService(
      CurrentUserService currentUserService,
      BearerTokenFactory bearerTokenFactory,
      PortalService portalService) {
    this.currentUserService = currentUserService;
    this.bearerTokenFactory = bearerTokenFactory;
    this.portalService = portalService;
  }

  /** gets the user from the request, throwing an exception if not present */
  public AdminUser getFromRequest(HttpServletRequest request) {
    String token = bearerTokenFactory.from(request).getToken();
    Optional<AdminUser> userOpt = currentUserService.findByToken(token);
    if (userOpt.isEmpty()) {
      throw new UnauthorizedException("User not found");
    }
    return userOpt.get();
  }

  public Portal authUserToPortal(AdminUser user, String portalShortcode) {
    return portalService.authUserToPortal(user, portalShortcode);
  }
}
