package bio.terra.pearl.api.admin.service.study;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.service.kit.StudyEnvironmentKitTypeService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.WithdrawnEnrolleeService;
import bio.terra.pearl.core.service.study.StudyEnvironmentConfigService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StudyEnvironmentExtService {
  private StudyEnvironmentService studyEnvService;
  private StudyEnvironmentConfigService studyEnvConfigService;
  private AuthUtilService authUtilService;
  private EnrolleeService enrolleeService;
  private WithdrawnEnrolleeService withdrawnEnrolleeService;
  private StudyEnvironmentKitTypeService studyEnvironmentKitTypeService;

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
  public StudyEnvironment update(
      AdminUser operator,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName envName,
      StudyEnvironment update) {
    StudyEnvironment studyEnv = authToStudyEnv(operator, portalShortcode, studyShortcode, envName);
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
    existing.setPasswordProtected(update.isPasswordProtected());
    existing.setAcceptingEnrollment(update.isAcceptingEnrollment());
    existing.setPassword(update.getPassword());
    return studyEnvConfigService.update(existing);
  }

  public List<KitType> getKitTypes(
      AdminUser operator, String portalShortcode, String studyShortcode, EnvironmentName envName) {
    StudyEnvironment studyEnv = authToStudyEnv(operator, portalShortcode, studyShortcode, envName);
    return studyEnvironmentKitTypeService.findKitTypesByStudyEnvironmentId(studyEnv.getStudyId());
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
