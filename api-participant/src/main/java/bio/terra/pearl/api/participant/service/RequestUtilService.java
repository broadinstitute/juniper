package bio.terra.pearl.api.participant.service;

import bio.terra.common.exception.UnauthorizedException;
import bio.terra.common.iam.BearerTokenFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.portal.PortalWithPortalUser;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import com.auth0.jwt.JWT;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class RequestUtilService {
  private BearerTokenFactory bearerTokenFactory;
  private CurrentUserService currentUserService;
  private StudyEnvironmentService studyEnvironmentService;
  private PortalService portalService;

  public RequestUtilService(
      BearerTokenFactory bearerTokenFactory,
      CurrentUserService currentUserService,
      StudyEnvironmentService studyEnvironmentService,
      PortalService portalService) {
    this.bearerTokenFactory = bearerTokenFactory;
    this.currentUserService = currentUserService;
    this.studyEnvironmentService = studyEnvironmentService;
    this.portalService = portalService;
  }

  /** gets the user from the request, throwing an exception if not present */
  public ParticipantUser userFromRequest(HttpServletRequest request) {
    var token = tokenFromRequest(request);
    var decodedJWT = JWT.decode(token);
    var email = decodedJWT.getClaim("email").asString();
    Optional<ParticipantUser> userOpt = currentUserService.findByUsername(email);
    if (userOpt.isEmpty()) {
      throw new UnauthorizedException("User not found");
    }
    return userOpt.get();
  }

  public Optional<ParticipantUser> userOptFromRequest(HttpServletRequest request) {
    String token = tokenFromRequest(request);
    return currentUserService.findByToken(token);
  }

  public PortalWithPortalUser authParticipantToPortal(
      UUID participantId, String portalShortcode, EnvironmentName envName) {
    return portalService.authParticipantToPortal(participantId, portalShortcode, envName);
  }

  public String tokenFromRequest(HttpServletRequest request) {
    return bearerTokenFactory.from(request).getToken();
  }

  public StudyEnvironment getStudyEnv(String studyShortcode, String envName) {
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    Optional<StudyEnvironment> studyEnv =
        studyEnvironmentService.findByStudy(studyShortcode, environmentName);
    if (studyEnv.isEmpty()) {
      throw new IllegalArgumentException("Study environment does not exist");
    }
    return studyEnv.get();
  }
}
