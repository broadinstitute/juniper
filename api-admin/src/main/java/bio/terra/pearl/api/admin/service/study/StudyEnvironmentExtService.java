package bio.terra.pearl.api.admin.service.study;

import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.EnforcePortalStudyEnvPermission;
import bio.terra.pearl.api.admin.service.auth.SandboxOnly;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.kit.StudyEnvironmentKitType;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.service.kit.StudyEnvironmentKitTypeService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.WithdrawnEnrolleeService;
import bio.terra.pearl.core.service.study.StudyEnvironmentConfigService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class StudyEnvironmentExtService {
  private final StudyEnvironmentService studyEnvService;
  private final StudyEnvironmentConfigService studyEnvConfigService;
  private final EnrolleeService enrolleeService;
  private final WithdrawnEnrolleeService withdrawnEnrolleeService;
  private final StudyEnvironmentKitTypeService studyEnvironmentKitTypeService;

  public StudyEnvironmentExtService(
      StudyEnvironmentService studyEnvService,
      StudyEnvironmentConfigService studyEnvConfigService,
      EnrolleeService enrolleeService,
      WithdrawnEnrolleeService withdrawnEnrolleeService,
      StudyEnvironmentKitTypeService studyEnvironmentKitTypeService) {
    this.studyEnvService = studyEnvService;
    this.studyEnvConfigService = studyEnvConfigService;
    this.enrolleeService = enrolleeService;
    this.withdrawnEnrolleeService = withdrawnEnrolleeService;
    this.studyEnvironmentKitTypeService = studyEnvironmentKitTypeService;
  }

  /** currently only supports changing the pre-enroll survey id */
  @SandboxOnly
  @EnforcePortalStudyEnvPermission(permission = "survey_edit")
  public StudyEnvironment update(PortalStudyEnvAuthContext authContext, StudyEnvironment update) {
    StudyEnvironment studyEnv = authContext.getStudyEnvironment();
    studyEnv.setPreEnrollSurveyId(update.getPreEnrollSurveyId());
    return studyEnvService.update(studyEnv);
  }

  @EnforcePortalStudyEnvPermission(permission = "study_settings_edit")
  public StudyEnvironmentConfig updateConfig(
      PortalStudyEnvAuthContext authContext, StudyEnvironmentConfig update) {
    StudyEnvironmentConfig existing =
        studyEnvConfigService
            .find(authContext.getStudyEnvironment().getStudyEnvironmentConfigId())
            .get();
    // we don't allow directly setting the 'initialized' field -- that comes from the publishing
    // flows
    BeanUtils.copyProperties(update, existing, "initialized", "id", "createdAt", "lastUpdatedAt");
    return studyEnvConfigService.update(existing);
  }

  @EnforcePortalStudyEnvPermission(permission = AuthUtilService.BASE_PERMISSON)
  public List<KitType> getKitTypes(PortalStudyEnvAuthContext authContext) {
    return studyEnvironmentKitTypeService.findKitTypesByStudyEnvironmentId(
        authContext.getStudyEnvironment().getId());
  }

  @SandboxOnly
  @EnforcePortalStudyEnvPermission(permission = "study_settings_edit")
  public void updateKitTypes(PortalStudyEnvAuthContext authContext, List<String> updatedKitTypes) {
    List<KitType> allowedKitTypes = studyEnvironmentKitTypeService.findAllowedKitTypes();
    validateKitTypes(updatedKitTypes, allowedKitTypes);

    List<KitType> configuredKitTypes =
        studyEnvironmentKitTypeService.findKitTypesByStudyEnvironmentId(
            authContext.getStudyEnvironment().getId());

    List<StudyEnvironmentKitType> newKitTypes = new ArrayList<>();
    for (String kitTypeName : updatedKitTypes) {
      KitType kitType =
          allowedKitTypes.stream()
              .filter(k -> k.getName().equals(kitTypeName))
              .findFirst()
              .orElseThrow(() -> new IllegalArgumentException("Invalid kit type"));
      StudyEnvironmentKitType studyEnvironmentKitType =
          StudyEnvironmentKitType.builder()
              .studyEnvironmentId(authContext.getStudyEnvironment().getId())
              .kitTypeId(kitType.getId())
              .build();
      newKitTypes.add(studyEnvironmentKitType);
    }

    List<StudyEnvironmentKitType> removedKitTypes = new ArrayList<>();
    for (KitType configuredKitType : configuredKitTypes) {
      if (!updatedKitTypes.contains(configuredKitType.getName())) {
        StudyEnvironmentKitType studyEnvironmentKitType =
            StudyEnvironmentKitType.builder()
                .studyEnvironmentId(authContext.getStudyEnvironment().getId())
                .kitTypeId(configuredKitType.getId())
                .build();
        removedKitTypes.add(studyEnvironmentKitType);
      }
    }

    removedKitTypes.forEach(
        kitType ->
            studyEnvironmentKitTypeService.deleteByKitTypeIdAndStudyEnvironmentId(
                kitType.getKitTypeId(), kitType.getStudyEnvironmentId()));
    studyEnvironmentKitTypeService.bulkCreate(newKitTypes);
  }

  @EnforcePortalStudyEnvPermission(permission = AuthUtilService.BASE_PERMISSON)
  public List<KitType> getAllowedKitTypes(PortalStudyEnvAuthContext authContext) {
    return studyEnvironmentKitTypeService.findAllowedKitTypes();
  }

  @EnforcePortalStudyEnvPermission(permission = AuthUtilService.BASE_PERMISSON)
  public StudyEnvStats getStats(PortalStudyEnvAuthContext authContext) {
    return new StudyEnvStats(
        enrolleeService.countByStudyEnvironmentId(authContext.getStudyEnvironment().getId()),
        withdrawnEnrolleeService.countByStudyEnvironmentId(
            authContext.getStudyEnvironment().getId()));
  }

  public record StudyEnvStats(int enrolleeCount, int withdrawnCount) {}

  protected void validateKitTypes(List<String> updatedKitTypes, List<KitType> allowedKitTypes) {
    Set<String> updatedKitTypeSet = new HashSet<>(updatedKitTypes);
    Set<String> allowedKitTypeSet =
        allowedKitTypes.stream().map(KitType::getName).collect(Collectors.toSet());

    if (!allowedKitTypeSet.containsAll(updatedKitTypeSet)) {
      throw new IllegalArgumentException("Invalid kit type");
    }
  }
}
