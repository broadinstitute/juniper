package bio.terra.pearl.api.participant.service;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.ResponseData;
import bio.terra.pearl.core.model.survey.ResponseSnapshotDto;
import bio.terra.pearl.core.model.survey.SurveyWithResponse;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.service.portal.PortalWithPortalUser;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SurveyResponseExtService {
  private AuthUtilService authUtilService;
  private RequestUtilService requestUtilService;
  private SurveyResponseService surveyResponseService;
  private ObjectMapper objectMapper;

  public SurveyResponseExtService(
      AuthUtilService authUtilService,
      RequestUtilService requestUtilService,
      SurveyResponseService surveyResponseService,
      ObjectMapper objectMapper) {
    this.authUtilService = authUtilService;
    this.requestUtilService = requestUtilService;
    this.surveyResponseService = surveyResponseService;
    this.objectMapper = objectMapper;
  }

  public SurveyWithResponse findOrCreateWithActiveResponse(
      String studyShortcode,
      String envName,
      String stableId,
      Integer version,
      String enrolleeShortcode,
      UUID participantUserId,
      UUID taskId) {

    Enrollee enrollee =
        authUtilService.authParticipantUserToEnrollee(participantUserId, enrolleeShortcode);
    StudyEnvironment studyEnv = requestUtilService.getStudyEnv(studyShortcode, envName);
    return surveyResponseService.findOrCreateWithActiveResponse(
        studyEnv.getId(), stableId, version, enrollee, participantUserId, taskId);
  }

  public HubResponse submitResponse(
      ParticipantUser user,
      String portalShortcode,
      EnvironmentName envName,
      ResponseSnapshotDto responseDto,
      String enrolleeShortcode,
      UUID taskId) {
    PortalWithPortalUser portalWithPortalUser =
        authUtilService.authParticipantToPortal(user.getId(), portalShortcode, envName);
    Enrollee enrollee =
        authUtilService.authParticipantUserToEnrollee(user.getId(), enrolleeShortcode);
    processResponseSnapshotDto(responseDto);
    HubResponse result =
        surveyResponseService.submitResponse(
            user.getId(), portalWithPortalUser.ppUser(), enrollee, taskId, responseDto);
    return result;
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
