package bio.terra.pearl.api.participant.controller.survey;

import bio.terra.pearl.api.participant.api.SurveyResponseApi;
import bio.terra.pearl.api.participant.service.RequestUtilService;
import bio.terra.pearl.api.participant.service.SurveyResponseExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.model.survey.SurveyWithResponse;
import bio.terra.pearl.core.model.workflow.HubResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class SurveyResponseController implements SurveyResponseApi {
  private SurveyResponseExtService surveyResponseExtService;
  private HttpServletRequest request;
  private RequestUtilService requestUtilService;
  private ObjectMapper objectMapper;

  public SurveyResponseController(
      SurveyResponseExtService surveyResponseExtService,
      HttpServletRequest request,
      RequestUtilService requestUtilService,
      ObjectMapper objectMapper) {
    this.surveyResponseExtService = surveyResponseExtService;
    this.request = request;
    this.requestUtilService = requestUtilService;
    this.objectMapper = objectMapper;
  }

  @Override
  public ResponseEntity<Object> formAndResponse(
      String portalShortcode,
      String envName,
      String studyShortcode,
      String enrolleeShortcode,
      String stableId,
      Integer version,
      UUID taskId) {
    ParticipantUser user = requestUtilService.requireUser(request);
    SurveyWithResponse result =
        surveyResponseExtService.findOrCreateWithActiveResponse(
            studyShortcode, envName, stableId, version, enrolleeShortcode, user.getId(), taskId);
    return ResponseEntity.ok(result);
  }

  @Override
  public ResponseEntity<Object> update(
      String portalShortcode,
      String envName,
      String studyShortcode,
      String enrolleeShortcode,
      String stableId,
      Integer version,
      UUID taskId,
      Object body) {
    ParticipantUser user = requestUtilService.requireUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    SurveyResponse responseDto = objectMapper.convertValue(body, SurveyResponse.class);
    HubResponse hubResponse =
        surveyResponseExtService.updateResponse(
            user, portalShortcode, environmentName, responseDto, enrolleeShortcode, taskId);
    return ResponseEntity.ok(hubResponse);
  }
}
