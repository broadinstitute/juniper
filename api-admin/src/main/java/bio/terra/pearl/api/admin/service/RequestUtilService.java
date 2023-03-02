package bio.terra.pearl.api.admin.service;

import bio.terra.common.exception.UnauthorizedException;
import bio.terra.common.iam.BearerTokenFactory;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.PortalStudy;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.study.PortalStudyService;
import com.auth0.jwt.JWT;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

/** Utility methods for handling requests */
@Service
public class RequestUtilService {
  private CurrentUserService currentUserService;
  private BearerTokenFactory bearerTokenFactory;
  private PortalService portalService;
  private PortalStudyService portalStudyService;
  private EnrolleeService enrolleeService;

  public RequestUtilService(
      CurrentUserService currentUserService,
      BearerTokenFactory bearerTokenFactory,
      PortalService portalService,
      PortalStudyService portalStudyService) {
    this.currentUserService = currentUserService;
    this.bearerTokenFactory = bearerTokenFactory;
    this.portalService = portalService;
    this.portalStudyService = portalStudyService;
  }

  /** gets the user from the request, throwing an exception if not present */
  public AdminUser getFromRequest(HttpServletRequest request) {
    String token = bearerTokenFactory.from(request).getToken();
    var decodedJWT = JWT.decode(token);
    var email = decodedJWT.getClaim("email").asString();
    Optional<AdminUser> userOpt = currentUserService.findByUsername(email);
    if (userOpt.isEmpty()) {
      throw new UnauthorizedException("User not found: " + email);
    }
    return userOpt.get();
  }

  public Portal authUserToPortal(AdminUser user, String portalShortcode) {
    return portalService.authAdminToPortal(user, portalShortcode);
  }

  public PortalStudy authUserToStudy(
      AdminUser user, String portalShortcode, String studyShortcode) {
    Portal portal = authUserToPortal(user, portalShortcode);
    Optional<PortalStudy> portalStudy =
        portalStudyService.findStudyInPortal(studyShortcode, portal.getId());
    if (portalStudy.isEmpty()) {
      throw new PermissionDeniedException(
          "User %s does not have permissions on study %s"
              .formatted(user.getUsername(), studyShortcode));
    }
    return portalStudy.get();
  }
}
