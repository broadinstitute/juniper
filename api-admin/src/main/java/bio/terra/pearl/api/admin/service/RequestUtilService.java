package bio.terra.pearl.api.admin.service;

import bio.terra.common.exception.UnauthorizedException;
import bio.terra.common.iam.BearerTokenFactory;
import bio.terra.common.iam.SamUserFactory;
import bio.terra.pearl.core.config.SamConfiguration;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.CurrentUserService;
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
  private SamConfiguration samConfiguration = new SamConfiguration("https://sam.dsde-dev.broadinstitute.org");
  private SamUserFactory samUserFactory;

  public RequestUtilService(
          CurrentUserService currentUserService,
          BearerTokenFactory bearerTokenFactory,
          PortalService portalService,
          SamUserFactory samUserFactory) {
    this.currentUserService = currentUserService;
    this.bearerTokenFactory = bearerTokenFactory;
    this.portalService = portalService;
    this.samUserFactory = samUserFactory;
  }

  /** gets the user from the request, throwing an exception if not present */
  public AdminUser getFromRequest(HttpServletRequest request) {
    var samUser = samUserFactory.from(request, samConfiguration.basePath());
    Optional<AdminUser> userOpt = currentUserService.unauthedLogin(samUser.getEmail());
    if (userOpt.isEmpty()) {
      throw new UnauthorizedException("User not found");
    }
    return userOpt.get();
  }

  public Portal authUserToPortal(AdminUser user, String portalShortcode) {
    return portalService.authUserToPortal(user, portalShortcode);
  }
}
