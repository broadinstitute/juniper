package bio.terra.pearl.api.admin.service.enrollee;

import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.EnforcePortalStudyEnvPermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.model.participant.RelationshipType;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.participant.EnrolleeRelationService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.FamilyService;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class EnrolleeRelationExtService {
  private final AuthUtilService authUtilService;
  private final EnrolleeRelationService enrolleeRelationService;
  private final EnrolleeService enrolleeService;
  private final FamilyService familyService;

  public EnrolleeRelationExtService(
      AuthUtilService authUtilService,
      EnrolleeRelationService enrolleeRelationService,
      EnrolleeService enrolleeService,
      FamilyService familyService) {
    this.authUtilService = authUtilService;
    this.enrolleeRelationService = enrolleeRelationService;
    this.enrolleeService = enrolleeService;
    this.familyService = familyService;
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
    return enrolleeRelationService.findByTargetEnrolleeIdWithEnrolleesAndFamily(enrollee.getId());
  }

  @EnforcePortalStudyEnvPermission(permission = "???")
  public EnrolleeRelation create(
      PortalStudyEnvAuthContext authContext, EnrolleeRelation relation, String justification) {

    // ensure the enrollees/families are in the expected study environment
    enrolleeService
        .find(relation.getEnrolleeId())
        .filter(e -> e.getStudyEnvironmentId().equals(authContext.getStudyEnvironment().getId()))
        .orElseThrow(() -> new NotFoundException("Enrollee not found"));
    enrolleeService
        .find(relation.getTargetEnrolleeId())
        .filter(e -> e.getStudyEnvironmentId().equals(authContext.getStudyEnvironment().getId()))
        .orElseThrow(() -> new NotFoundException("Enrollee not found"));

    if (Objects.nonNull(relation.getFamilyId())) {
      familyService
          .find(relation.getFamilyId())
          .filter(f -> f.getStudyEnvironmentId().equals(authContext.getStudyEnvironment().getId()))
          .orElseThrow(() -> new NotFoundException("Family not found"));
    }
    DataAuditInfo auditInfo =
        DataAuditInfo.builder()
            .responsibleAdminUserId(authContext.getOperator().getId())
            .enrolleeId(relation.getTargetEnrolleeId())
            .justification(justification)
            .build();

    if (relation.getRelationshipType().equals(RelationshipType.FAMILY)) {
      return enrolleeRelationService.createFamilyRelationship(relation, auditInfo);
    } else {
      // for now, let's only allow creating family relationships
      throw new IllegalArgumentException("Can only create family relationships");
    }
  }

  @EnforcePortalStudyEnvPermission(permission = "???")
  public void delete(
      PortalStudyEnvAuthContext authContext, UUID enrolleeRelationId, String justification) {
    EnrolleeRelation relation =
        enrolleeRelationService
            .find(enrolleeRelationId)
            .orElseThrow(() -> new NotFoundException("Enrollee relation not found"));

    Enrollee enrollee =
        enrolleeService
            .find(relation.getEnrolleeId())
            .orElseThrow(() -> new IllegalStateException("Invalid enrollee relation"));

    if (!enrollee.getStudyEnvironmentId().equals(authContext.getStudyEnvironment().getId())) {
      throw new NotFoundException("Enrollee relation not found");
    }

    DataAuditInfo auditInfo =
        DataAuditInfo.builder()
            .responsibleAdminUserId(authContext.getOperator().getId())
            .enrolleeId(relation.getTargetEnrolleeId())
            .justification(justification)
            .build();

    enrolleeRelationService.delete(relation.getId(), auditInfo);
  }
}
