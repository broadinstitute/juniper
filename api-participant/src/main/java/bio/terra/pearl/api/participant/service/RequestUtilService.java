package bio.terra.pearl.api.participant.service;

import bio.terra.common.exception.UnauthorizedException;
import bio.terra.common.iam.BearerTokenFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class RequestUtilService {
  private BearerTokenFactory bearerTokenFactory;
  private CurrentUserService currentUserService;
  private StudyEnvironmentService studyEnvironmentService;

  public static final Pattern ENVIRONMENT_NAME_PATTERN = Pattern.compile("\\/env\\/([a-zA-Z]+)\\/");

  public RequestUtilService(
      BearerTokenFactory bearerTokenFactory,
      CurrentUserService currentUserService,
      StudyEnvironmentService studyEnvironmentService) {
    this.bearerTokenFactory = bearerTokenFactory;
    this.currentUserService = currentUserService;
    this.studyEnvironmentService = studyEnvironmentService;
  }

  /** gets the user from the request, throwing an exception if not present */
  public ParticipantUser requireUser(HttpServletRequest request) {
    String token = requireToken(request);
    Optional<ParticipantUser> userOpt = getUserFromRequest(request);
    if (userOpt.isEmpty()) {
      throw new UnauthorizedException("User not found");
    }
    return userOpt.get();
  }

  public Optional<ParticipantUser> getUserFromRequest(HttpServletRequest request) {
    String token = tokenFromRequest(request);
    if (token == null) {
      return Optional.empty();
    }
    return getUserFromToken(request, token);
  }

  protected Optional<ParticipantUser> getUserFromToken(HttpServletRequest request, String token) {
    EnvironmentName envName = environmentNameFromRequest(request);
    String email = currentUserService.getUsernameFromToken(token);
    return currentUserService.findByUsername(email, envName);
  }

  public EnvironmentName environmentNameFromRequest(HttpServletRequest request) {
    Matcher matcher = ENVIRONMENT_NAME_PATTERN.matcher(request.getRequestURI());
    matcher.find();
    return EnvironmentName.valueOfCaseInsensitive(matcher.group(1));
  }

  public String requireToken(HttpServletRequest request) {
    return bearerTokenFactory.from(request).getToken();
  }

  public String tokenFromRequest(HttpServletRequest request) {
    try {
      return bearerTokenFactory.from(request).getToken();
    } catch (Exception e) {
      return null;
    }
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
