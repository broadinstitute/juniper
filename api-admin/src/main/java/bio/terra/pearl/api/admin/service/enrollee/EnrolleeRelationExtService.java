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
import bio.terra.pearl.core.service.participant.FamilyEnrolleeService;
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
  private final FamilyEnrolleeService familyEnrolleeService;

  public EnrolleeRelationExtService(
      AuthUtilService authUtilService,
      EnrolleeRelationService enrolleeRelationService,
      EnrolleeService enrolleeService,
      FamilyService familyService,
      FamilyEnrolleeService familyEnrolleeService) {
    this.authUtilService = authUtilService;
    this.enrolleeRelationService = enrolleeRelationService;
    this.enrolleeService = enrolleeService;
    this.familyService = familyService;
    this.familyEnrolleeService = familyEnrolleeService;
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

  // todo
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

    if (relation.getRelationshipType().equals(RelationshipType.PROXY)) {
      // for now, let's only allow creating family relationships
      throw new IllegalArgumentException("Cannot create proxy relationships");
    }

    if (relation.getRelationshipType().equals(RelationshipType.FAMILY)) {
      if (Objects.isNull(relation.getFamilyId())) {
        throw new IllegalArgumentException("Family ID is required for family relationships");
      }

      // ensure that the enrollees are in the family
      familyEnrolleeService.getOrCreate(
          relation.getTargetEnrolleeId(),
          relation.getFamilyId(),
          DataAuditInfo.builder()
              .responsibleAdminUserId(authContext.getOperator().getId())
              .enrolleeId(relation.getTargetEnrolleeId())
              .build());

      familyEnrolleeService.getOrCreate(
          relation.getEnrolleeId(),
          relation.getFamilyId(),
          DataAuditInfo.builder()
              .responsibleAdminUserId(authContext.getOperator().getId())
              .enrolleeId(relation.getEnrolleeId())
              .build());
    }

    // ensure that the relationship does not already exist
    if (enrolleeRelationService
        .findByEnrolleeIdAndRelationType(relation.getEnrolleeId(), relation.getRelationshipType())
        .stream()
        .anyMatch(r -> r.getTargetEnrolleeId().equals(relation.getTargetEnrolleeId()))) {
      throw new IllegalArgumentException("Enrollee relation already exists");
    }

    DataAuditInfo auditInfo =
        DataAuditInfo.builder()
            .responsibleAdminUserId(authContext.getOperator().getId())
            .enrolleeId(relation.getTargetEnrolleeId())
            .justification(justification)
            .build();

    // finally, create the relationship
    EnrolleeRelation created = enrolleeRelationService.create(relation, auditInfo);
    enrolleeRelationService.attachEnrolleesAndFamily(created);
    return created;
  }

  // todo
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
