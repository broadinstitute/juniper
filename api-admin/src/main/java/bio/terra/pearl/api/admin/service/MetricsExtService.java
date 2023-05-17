package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.core.dao.metrics.MetricName;
import bio.terra.pearl.core.dao.metrics.MetricsDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.metrics.BasicMetricDatum;
import bio.terra.pearl.core.model.metrics.TimeRange;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MetricsExtService {
  private AuthUtilService authUtilService;
  private StudyEnvironmentService studyEnvironmentService;
  private MetricsDao metricsDao;

  public MetricsExtService(
      AuthUtilService authUtilService,
      StudyEnvironmentService studyEnvironmentService,
      MetricsDao metricsDao) {
    this.authUtilService = authUtilService;
    this.studyEnvironmentService = studyEnvironmentService;
    this.metricsDao = metricsDao;
  }

  public List<BasicMetricDatum> loadMetrics(
      AdminUser user,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      MetricName metricName) {
    authUtilService.authUserToStudy(user, portalShortcode, studyShortcode);
    StudyEnvironment studyEnv =
        studyEnvironmentService.findByStudy(studyShortcode, environmentName).get();
    if (MetricName.STUDY_ENROLLMENT.equals(metricName)) {
      return metricsDao.studyEnrollments(studyEnv.getId(), new TimeRange(null, null));
    } else if (MetricName.STUDY_ENROLLEE_CONSENTED.equals(metricName)) {
      return metricsDao.studyConsentedEnrollees(studyEnv.getId(), new TimeRange(null, null));
    } else if (MetricName.STUDY_SURVEY_COMPLETION.equals(metricName)) {
      return metricsDao.studySurveyCompletions(studyEnv.getId(), new TimeRange(null, null));
    } else if (MetricName.STUDY_REQUIRED_SURVEY_COMPLETION.equals(metricName)) {
      return metricsDao.studyRequiredSurveyCompletions(studyEnv.getId(), new TimeRange(null, null));
    }
    throw new IllegalArgumentException("Unrecognized metric name '" + metricName + "'");
  }
}
