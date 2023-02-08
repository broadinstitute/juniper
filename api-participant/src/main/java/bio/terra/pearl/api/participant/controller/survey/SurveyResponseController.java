package bio.terra.pearl.api.participant.controller.survey;

import bio.terra.pearl.api.participant.api.SurveyResponseApi;
import bio.terra.pearl.api.participant.service.RequestUtilService;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.ResponseData;
import bio.terra.pearl.core.model.survey.ResponseSnapshotDto;
import bio.terra.pearl.core.model.survey.SurveyWithResponse;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class SurveyResponseController implements SurveyResponseApi {
  private SurveyResponseService surveyResponseService;
  private HttpServletRequest request;
  private RequestUtilService requestUtilService;
  private ObjectMapper objectMapper;

  public SurveyResponseController(
      SurveyResponseService surveyResponseService,
      HttpServletRequest request,
      RequestUtilService requestUtilService,
      ObjectMapper objectMapper) {
    this.surveyResponseService = surveyResponseService;
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
    ParticipantUser user = requestUtilService.userFromRequest(request);
    StudyEnvironment studyEnv = requestUtilService.getStudyEnv(studyShortcode, envName);
    SurveyWithResponse result =
        surveyResponseService.findWithActiveResponse(
            studyEnv.getId(), stableId, version, enrolleeShortcode, user.getId(), taskId);
    return ResponseEntity.ok(result);
  }

  @Override
  public ResponseEntity<Object> response(
      String portalShortcode,
      String envName,
      String studyShortcode,
      String enrolleeShortcode,
      String stableId,
      Integer version,
      UUID taskId,
      Object body) {
    ParticipantUser user = requestUtilService.userFromRequest(request);
    ResponseSnapshotDto response = objectMapper.convertValue(body, ResponseSnapshotDto.class);
    processResponseSnapshotDto(response);
    HubResponse result =
        surveyResponseService.submitResponse(
            portalShortcode, user.getId(), enrolleeShortcode, taskId, response);
    return ResponseEntity.ok(result);
  }

  /** the frontend might pass either parsed or string data back, handle either case */
  public void processResponseSnapshotDto(ResponseSnapshotDto response) {
    try {
      if (response.getFullData() == null && response.getParsedData() != null) {
        response.setFullData(objectMapper.writeValueAsString(response.getParsedData()));
      }
      if (response.getParsedData() == null && response.getFullData() != null) {
        response.setParsedData(objectMapper.readValue(response.getFullData(), ResponseData.class));
      }
    } catch (JsonProcessingException jpe) {
      throw new IllegalArgumentException("Could not process response:", jpe);
    }
  }
}
