package bio.terra.pearl.api.participant.service;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.consent.ConsentResponseDto;
import bio.terra.pearl.core.model.consent.ConsentWithResponses;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.service.consent.ConsentResponseService;
import bio.terra.pearl.core.service.portal.PortalWithPortalUser;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ConsentResponseExtService {
  private AuthUtilService authUtilService;
  private ConsentResponseService consentResponseService;
  private RequestUtilService requestUtilService;

  public ConsentResponseExtService(
      AuthUtilService authUtilService,
      ConsentResponseService consentResponseService,
      RequestUtilService requestUtilService) {
    this.authUtilService = authUtilService;
    this.consentResponseService = consentResponseService;
    this.requestUtilService = requestUtilService;
  }

  public ConsentWithResponses findWithResponses(
      String studyShortcode,
      String envName,
      String stableId,
      Integer version,
      String enrolleeShortcode,
      UUID participantUserId) {
    StudyEnvironment studyEnv = requestUtilService.getStudyEnv(studyShortcode, envName);
    Enrollee enrollee =
        authUtilService.authParticipantUserToEnrollee(participantUserId, enrolleeShortcode);
    return consentResponseService.findWithResponses(
        studyEnv.getId(), stableId, version, enrollee, participantUserId);
  }

  public HubResponse submitResponse(
      String portalShortcode,
      EnvironmentName envName,
      String enrolleeShortcode,
      ConsentResponseDto responseDto,
      UUID participantUserId) {
    PortalWithPortalUser portalWithPortalUser =
        authUtilService.authParticipantToPortal(participantUserId, portalShortcode, envName);
    Enrollee enrollee =
        authUtilService.authParticipantUserToEnrollee(participantUserId, enrolleeShortcode);
    HubResponse result =
        consentResponseService.submitResponse(
            participantUserId, portalWithPortalUser.ppUser(), enrollee, responseDto);
    return result;
  }
}
