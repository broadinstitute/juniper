package bio.terra.pearl.api.participant.controller.survey;

import bio.terra.pearl.api.participant.api.AnswerMappingApi;
import bio.terra.pearl.api.participant.service.AuthUtilService;
import bio.terra.pearl.api.participant.service.RequestUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.survey.AnswerMapping;
import bio.terra.pearl.core.model.survey.AnswerMappingTargetType;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.survey.AnswerMappingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class AnswerMappingController implements AnswerMappingApi {
  private final AnswerMappingService answerMappingService;
  private final HttpServletRequest request;
  private final RequestUtilService requestUtilService;
  private final AuthUtilService authUtilService;
  private final ObjectMapper objectMapper;

  public AnswerMappingController(
      AnswerMappingService answerMappingService,
      HttpServletRequest request,
      RequestUtilService requestUtilService,
      AuthUtilService authUtilService,
      ObjectMapper objectMapper) {
    this.answerMappingService = answerMappingService;
    this.request = request;
    this.requestUtilService = requestUtilService;
    this.authUtilService = authUtilService;
    this.objectMapper = objectMapper;
  }

  @Override
  public ResponseEntity<Object> findAnswerMapping(
      String portalShortcode,
      String envName,
      String stableId,
      Integer version,
      String targetType,
      String targetField) {
    ParticipantUser user = requestUtilService.requireUser(request);

    // user has access to given portal
    authUtilService.authParticipantToPortal(
        user.getId(), portalShortcode, EnvironmentName.valueOfCaseInsensitive(envName));

    AnswerMapping mapping =
        answerMappingService
            .findByTargetField(
                portalShortcode,
                stableId,
                version,
                AnswerMappingTargetType.valueOf(targetType),
                targetField)
            .orElseThrow(() -> new NotFoundException("Proxy question not found"));

    return ResponseEntity.ok(mapping);
  }
}
