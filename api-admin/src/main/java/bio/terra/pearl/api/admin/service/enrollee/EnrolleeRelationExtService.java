package bio.terra.pearl.api.admin.service.enrollee;

import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.participant.EnrolleeRelationService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EnrolleeRelationExtService {
  private final AuthUtilService authUtilService;
  private final EnrolleeRelationService enrolleeRelationService;
  private final EnrolleeService enrolleeService;
  private final StudyEnvironmentService studyEnvironmentService;

  public EnrolleeRelationExtService(
      AuthUtilService authUtilService,
      EnrolleeRelationService enrolleeRelationService,
      EnrolleeService enrolleeService,
      StudyEnvironmentService studyEnvironmentService) {
    this.authUtilService = authUtilService;
    this.enrolleeRelationService = enrolleeRelationService;
    this.enrolleeService = enrolleeService;
    this.studyEnvironmentService = studyEnvironmentService;
  }

  public List<EnrolleeRelation> findRelationsForTargetEnrollee(
      AdminUser operator,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      String enrolleeShortcode) {
    authUtilService.authUserToStudy(operator, portalShortcode, studyShortcode);

    Enrollee enrollee =
        enrolleeService
            .findByShortcodeAndStudyEnv(enrolleeShortcode, studyShortcode, environmentName)
            .orElseThrow(() -> new NotFoundException("Enrollee not found"));
    return enrolleeRelationService.findByTargetEnrolleeIdWithEnrollees(enrollee.getId());
  }
}
