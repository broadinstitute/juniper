package bio.terra.pearl.api.admin.service.enrollee;

import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.EnforcePortalEnrolleePermission;
import bio.terra.pearl.api.admin.service.auth.EnforcePortalStudyEnvPermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnrolleeAuthContext;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.model.audit.ParticipantDataChange;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeWithdrawalReason;
import bio.terra.pearl.core.model.participant.WithdrawnEnrollee;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.WithdrawnEnrolleeService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.workflow.ParticipantDataChangeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EnrolleeExtService {
  private final AuthUtilService authUtilService;
  private final EnrolleeService enrolleeService;
  private final WithdrawnEnrolleeService withdrawnEnrolleeService;
  private final ParticipantDataChangeService participantDataChangeService;
  private final StudyEnvironmentService studyEnvironmentService;

  public EnrolleeExtService(
      AuthUtilService authUtilService,
      EnrolleeService enrolleeService,
      WithdrawnEnrolleeService withdrawnEnrolleeService,
      ParticipantDataChangeService participantDataChangeService,
      StudyEnvironmentService studyEnvironmentService) {
    this.authUtilService = authUtilService;
    this.enrolleeService = enrolleeService;
    this.withdrawnEnrolleeService = withdrawnEnrolleeService;
    this.participantDataChangeService = participantDataChangeService;
    this.studyEnvironmentService = studyEnvironmentService;
  }

  @EnforcePortalStudyEnvPermission(permission = "participant_data_view")
  public List<Enrollee> findForKitManagement(PortalStudyEnvAuthContext authContext) {
    return enrolleeService.findForKitManagement(
        authContext.getStudyShortcode(), authContext.getEnvironmentName());
  }

  @EnforcePortalEnrolleePermission(permission = "participant_data_view")
  public Enrollee findWithAdminLoad(PortalEnrolleeAuthContext authContext) {
    return enrolleeService.loadForAdminView(authContext.getEnrollee());
  }

  @EnforcePortalEnrolleePermission(permission = "participant_data_view")
  public List<ParticipantDataChange> findDataChangeRecords(
      PortalEnrolleeAuthContext authContext, String modelName) {
    if (modelName != null) {
      return participantDataChangeService.findAllRecordsForEnrolleeAndModelName(
          authContext.getEnrollee(), modelName);
    }
    return participantDataChangeService.findAllRecordsForEnrollee(authContext.getEnrollee());
  }

  @EnforcePortalEnrolleePermission(permission = "participant_data_edit")
  public WithdrawnEnrollee withdrawEnrollee(
      PortalEnrolleeAuthContext authContext, EnrolleeWithdrawalReason reason, String note)
      throws JsonProcessingException {
    return withdrawnEnrolleeService.withdrawEnrollee(
        authContext.getEnrollee(), reason, note, authContext.dataAuditInfo());
  }
}
