package bio.terra.pearl.api.admin.service.enrollee;

import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.ParticipantDataChange;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.WithdrawnEnrollee;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.WithdrawnEnrolleeService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.workflow.ParticipantDataChangeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

  public List<Enrollee> findForKitManagement(
      AdminUser operator,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName) {
    authUtilService.authUserToStudy(operator, portalShortcode, studyShortcode);
    return enrolleeService.findForKitManagement(studyShortcode, environmentName);
  }

  public Enrollee findWithAdminLoad(
      AdminUser operator,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName envName,
      String enrolleeShortcodeOrId) {
    String enrolleeShortcode = enrolleeShortcodeOrId;
    if (enrolleeShortcode != null && enrolleeShortcode.length() > 16) {
      // it's an id, not a shortcode
      enrolleeShortcode =
          enrolleeService
              .find(UUID.fromString(enrolleeShortcode))
              .orElseThrow(
                  () ->
                      new NotFoundException(
                          "User %s does not have permissions on enrollee %s or enrollee does not exist"
                              .formatted(operator.getUsername(), enrolleeShortcodeOrId)))
              .getShortcode();
    }
    Optional<StudyEnvironment> studyEnvironment =
        studyEnvironmentService.findByStudy(studyShortcode, envName);
    if (studyEnvironment.isEmpty()) {
      throw new NotFoundException("Study environment not found");
    }
    Enrollee enrollee = authUtilService.authAdminUserToEnrollee(operator, enrolleeShortcode);
    if (!studyEnvironment.get().getId().equals(enrollee.getStudyEnvironmentId())) {
      throw new NotFoundException("Enrollee does not exist in this study environment");
    }
    return enrolleeService.loadForAdminView(enrollee);
  }

  public List<ParticipantDataChange> findDataChangeRecords(
      AdminUser operator, String enrolleeShortcode, String modelName) {
    Enrollee enrollee = authUtilService.authAdminUserToEnrollee(operator, enrolleeShortcode);
    if (modelName != null) {
      return participantDataChangeService.findAllRecordsForEnrolleeAndModelName(
          enrollee, modelName);
    }
    return participantDataChangeService.findAllRecordsForEnrollee(enrollee);
  }

  public WithdrawnEnrollee withdrawEnrollee(AdminUser operator, String enrolleeShortcode)
      throws JsonProcessingException {
    Enrollee enrollee = authUtilService.authAdminUserToEnrollee(operator, enrolleeShortcode);
    return withdrawnEnrolleeService.withdrawEnrollee(
        enrollee, DataAuditInfo.builder().responsibleAdminUserId(operator.getId()).build());
  }
}
