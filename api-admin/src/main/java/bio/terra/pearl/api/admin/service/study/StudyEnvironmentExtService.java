package bio.terra.pearl.api.admin.service.study;

import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.EnforcePortalStudyEnvPermission;
import bio.terra.pearl.api.admin.service.auth.SandboxOnly;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
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
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class StudyEnvironmentExtService {
  private final StudyEnvironmentService studyEnvService;
  private final StudyEnvironmentConfigService studyEnvConfigService;
  private final AuthUtilService authUtilService;
  private final EnrolleeService enrolleeService;
  private final WithdrawnEnrolleeService withdrawnEnrolleeService;
  private final StudyEnvironmentKitTypeService studyEnvironmentKitTypeService;

  public StudyEnvironmentExtService(
      StudyEnvironmentService studyEnvService,
      StudyEnvironmentConfigService studyEnvConfigService,
      AuthUtilService authUtilService,
      EnrolleeService enrolleeService,
      WithdrawnEnrolleeService withdrawnEnrolleeService,
      StudyEnvironmentKitTypeService studyEnvironmentKitTypeService) {
    this.studyEnvService = studyEnvService;
    this.studyEnvConfigService = studyEnvConfigService;
    this.authUtilService = authUtilService;
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

  public StudyEnvironmentConfig updateConfig(
      AdminUser operator,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName envName,
      StudyEnvironmentConfig update) {
    StudyEnvironment studyEnv = authToStudyEnv(operator, portalShortcode, studyShortcode, envName);
    StudyEnvironmentConfig existing =
        studyEnvConfigService.find(studyEnv.getStudyEnvironmentConfigId()).get();
    // we don't allow directly setting the 'initialized' field -- that comes from the publishing
    // flows
    BeanUtils.copyProperties(update, existing, "initialized", "id", "createdAt", "lastUpdatedAt");
    return studyEnvConfigService.update(existing);
  }

  // todo update this to use the new auth annotations
  public List<KitType> getKitTypes(
      AdminUser operator, String portalShortcode, String studyShortcode, EnvironmentName envName) {
    StudyEnvironment studyEnv = authToStudyEnv(operator, portalShortcode, studyShortcode, envName);
    return studyEnvironmentKitTypeService.findKitTypesByStudyEnvironmentId(studyEnv.getId());
  }

  public void updateKitTypes(
      AdminUser operator,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName envName,
      List<String> updatedKitTypes) {
    StudyEnvironment studyEnv = authToStudyEnv(operator, portalShortcode, studyShortcode, envName);
    List<KitType> allowedKitTypes = studyEnvironmentKitTypeService.findAllKitTypes();
    List<KitType> existingKitTypes =
        studyEnvironmentKitTypeService.findKitTypesByStudyEnvironmentId(studyEnv.getId());

    if (!new HashSet<>(updatedKitTypes)
        .containsAll(existingKitTypes.stream().map(KitType::getName).toList())) {
      throw new IllegalArgumentException("You may not remove a kit type from a study environment");
    }

    if (!new HashSet<>(allowedKitTypes.stream().map(KitType::getName).toList())
        .containsAll(updatedKitTypes)) {
      throw new IllegalArgumentException("Invalid kit type");
    }

    List<StudyEnvironmentKitType> newKitTypes = new ArrayList<>();
    for (String kitTypeName : updatedKitTypes) {
      KitType kitType =
          allowedKitTypes.stream().filter(k -> k.getName().equals(kitTypeName)).findFirst().get();
      StudyEnvironmentKitType foo =
          StudyEnvironmentKitType.builder()
              .studyEnvironmentId(studyEnv.getId())
              .kitTypeId(kitType.getId())
              .build();
      newKitTypes.add(foo);
    }

    studyEnvironmentKitTypeService.bulkCreate(newKitTypes);
  }

  public List<KitType> getAllKitTypes(
      AdminUser operator, String portalShortcode, String studyShortcode, EnvironmentName envName) {
    authToStudyEnv(operator, portalShortcode, studyShortcode, envName);
    return studyEnvironmentKitTypeService.findAllKitTypes();
  }

  public StudyEnvStats getStats(
      AdminUser operator, String portalShortcode, String studyShortcode, EnvironmentName envName) {
    StudyEnvironment studyEnv = authToStudyEnv(operator, portalShortcode, studyShortcode, envName);
    return new StudyEnvStats(
        enrolleeService.countByStudyEnvironmentId(studyEnv.getId()),
        withdrawnEnrolleeService.countByStudyEnvironmentId(studyEnv.getId()));
  }

  private StudyEnvironment authToStudyEnv(
      AdminUser operator, String portalShortcode, String studyShortcode, EnvironmentName envName) {
    authUtilService.authUserToStudy(operator, portalShortcode, studyShortcode);
    return studyEnvService.findByStudy(studyShortcode, envName).get();
  }

  public record StudyEnvStats(int enrolleeCount, int withdrawnCount) {}
}
