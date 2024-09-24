package bio.terra.pearl.api.participant.service;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.model.survey.SurveyWithResponse;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.service.portal.PortalWithPortalUser;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SurveyResponseExtService {
  private final AuthUtilService authUtilService;
  private final RequestUtilService requestUtilService;
  private final SurveyResponseService surveyResponseService;
  private final ObjectMapper objectMapper;

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
      String portalShortcode,
      String studyShortcode,
      String envName,
      String stableId,
      Integer version,
      String enrolleeShortcode,
      UUID participantUserId,
      UUID taskId) {

    Enrollee enrollee =
        authUtilService.authParticipantUserToEnrollee(participantUserId, enrolleeShortcode);
    Portal portal =
        authUtilService
            .authParticipantToPortal(
                participantUserId, portalShortcode, EnvironmentName.valueOf(envName))
            .portal();
    StudyEnvironment studyEnv = requestUtilService.getStudyEnv(studyShortcode, envName);
    return surveyResponseService.findWithActiveResponse(
        studyEnv.getId(), portal.getId(), stableId, version, enrollee, taskId);
  }

  public HubResponse updateResponse(
      ParticipantUser user,
      String portalShortcode,
      EnvironmentName envName,
      SurveyResponse response,
      String enrolleeShortcode,
      UUID taskId) {
    Enrollee enrollee =
        authUtilService.authParticipantUserToEnrollee(user.getId(), enrolleeShortcode);
    PortalWithPortalUser portalWithOperatorUser =
        authUtilService.authParticipantToPortal(user.getId(), portalShortcode, envName);
    HubResponse result =
        surveyResponseService.updateResponse(
            response,
            new ResponsibleEntity(user),
            null,
            portalWithOperatorUser.ppUser(),
            enrollee,
            taskId,
            portalWithOperatorUser.portal().getId());
    return result;
  }
}
