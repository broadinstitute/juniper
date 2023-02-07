package bio.terra.pearl.api.participant.controller.survey;

import bio.terra.pearl.api.participant.api.SurveyResponseApi;
import bio.terra.pearl.api.participant.service.RequestUtilService;
import bio.terra.pearl.core.model.consent.ConsentResponseDto;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.SurveyWithResponse;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
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
    /**
     * for now, we ignore the taskId. Later, we might want to validate that the task is still valid
     * before we return all the data so that the participant doesn't fill out an irrelevant form.
     * Not validating the task also makes it easier to spot-check survey and consent UX without
     * specific test users
     */
    ParticipantUser user = requestUtilService.userFromRequest(request);
    ConsentResponseDto response = objectMapper.convertValue(body, ConsentResponseDto.class);
    //    HubResponse result =
    //        consentResponseService.submitResponse(
    //            portalShortcode, user.getId(), enrolleeShortcode, taskId, response);
    return null; // ResponseEntity.ok(result);
  }
}
