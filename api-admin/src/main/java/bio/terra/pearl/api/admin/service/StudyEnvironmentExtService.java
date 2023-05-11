package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.WithdrawnEnrolleeService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import org.springframework.stereotype.Service;

@Service
public class StudyEnvironmentExtService {
  private StudyEnvironmentService studyEnvService;
  private AuthUtilService authUtilService;
  private EnrolleeService enrolleeService;
  private WithdrawnEnrolleeService withdrawnEnrolleeService;

  public StudyEnvironmentExtService(
      StudyEnvironmentService studyEnvService,
      AuthUtilService authUtilService,
      EnrolleeService enrolleeService,
      WithdrawnEnrolleeService withdrawnEnrolleeService) {
    this.studyEnvService = studyEnvService;
    this.authUtilService = authUtilService;
    this.enrolleeService = enrolleeService;
    this.withdrawnEnrolleeService = withdrawnEnrolleeService;
  }

  /** currently only supports changing the pre-enroll survey id */
  public StudyEnvironment update(
      AdminUser user, String portalShortcode, String studyShortcode, StudyEnvironment update) {
    authUtilService.authUserToStudy(user, portalShortcode, studyShortcode);
    StudyEnvironment existing = studyEnvService.find(update.getId()).get();
    existing.setPreEnrollSurveyId(update.getPreEnrollSurveyId());
    return studyEnvService.update(existing);
  }

  public StudyEnvStats getStats(
      AdminUser user,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName) {
    authUtilService.authUserToStudy(user, portalShortcode, studyShortcode);
    StudyEnvironment studyEnv = studyEnvService.findByStudy(studyShortcode, environmentName).get();
    return new StudyEnvStats(
        enrolleeService.countByStudyEnvironmentId(studyEnv.getId()),
        withdrawnEnrolleeService.countByStudyEnvironmentId(studyEnv.getId()));
  }

  public record StudyEnvStats(int enrolleeCount, int withdrawnCount) {}
}
