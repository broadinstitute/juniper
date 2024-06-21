package bio.terra.pearl.api.admin.service.forms;

import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.EnforcePortalStudyEnvPermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.survey.SurveyResponseDto;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SurveyResponseExtService {
  private final AuthUtilService authUtilService;
  private final PortalParticipantUserService portalParticipantUserService;
  private final SurveyResponseService surveyResponseService;

  public SurveyResponseExtService(
      AuthUtilService authUtilService,
      PortalParticipantUserService portalParticipantUserService,
      SurveyResponseService surveyResponseService) {
    this.authUtilService = authUtilService;
    this.portalParticipantUserService = portalParticipantUserService;
    this.surveyResponseService = surveyResponseService;
  }

  @EnforcePortalStudyEnvPermission(permission = "survey_response_edit")
  public HubResponse updateResponse(
      PortalStudyEnvAuthContext authContext,
      AdminUser user,
      SurveyResponseDto responseDto,
      String enrolleeShortcode,
      UUID taskId) {
    Portal portal = authContext.getPortal();
    Enrollee enrollee = authUtilService.authAdminUserToEnrollee(user, enrolleeShortcode);
    PortalParticipantUser ppUser = portalParticipantUserService.findForEnrollee(enrollee);

    HubResponse result =
        surveyResponseService.updateResponse(
            responseDto, new ResponsibleEntity(user), ppUser, enrollee, taskId, portal.getId());
    return result;
  }
}
