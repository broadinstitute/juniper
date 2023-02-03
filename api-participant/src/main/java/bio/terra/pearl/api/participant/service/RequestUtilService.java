package bio.terra.pearl.api.participant.service;

import bio.terra.common.exception.UnauthorizedException;
import bio.terra.common.iam.BearerTokenFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class RequestUtilService {
  private BearerTokenFactory bearerTokenFactory;
  private CurrentUserService currentUserService;
  private StudyEnvironmentService studyEnvironmentService;

  public RequestUtilService(
      BearerTokenFactory bearerTokenFactory,
      CurrentUserService currentUserService,
      StudyEnvironmentService studyEnvironmentService) {
    this.bearerTokenFactory = bearerTokenFactory;
    this.currentUserService = currentUserService;
    this.studyEnvironmentService = studyEnvironmentService;
  }

  /** gets the user from the request, throwing an exception if not present */
  public ParticipantUser userFromRequest(HttpServletRequest request) {
    Optional<ParticipantUser> userOpt = userOptFromRequest(request);
    if (userOpt.isEmpty()) {
      throw new UnauthorizedException("User not found");
    }
    return userOpt.get();
  }

  public Optional<ParticipantUser> userOptFromRequest(HttpServletRequest request) {
    String token = tokenFromRequest(request);
    return currentUserService.findByToken(token);
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
