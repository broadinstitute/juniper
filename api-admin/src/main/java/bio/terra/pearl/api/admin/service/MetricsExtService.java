package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.core.dao.metrics.MetricName;
import bio.terra.pearl.core.dao.metrics.MetricsDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.metrics.BasicMetricDatum;
import bio.terra.pearl.core.model.metrics.SurveyAnswerDatum;
import bio.terra.pearl.core.model.metrics.TimeRange;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.survey.AnswerService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class MetricsExtService {
  private AuthUtilService authUtilService;
  private StudyEnvironmentService studyEnvironmentService;
  private AnswerService answerService;
  private EnrolleeService enrolleeService;
  private MetricsDao metricsDao;

  public MetricsExtService(
      AuthUtilService authUtilService,
      StudyEnvironmentService studyEnvironmentService,
      AnswerService answerService,
      EnrolleeService enrolleeService,
      MetricsDao metricsDao) {
    this.authUtilService = authUtilService;
    this.studyEnvironmentService = studyEnvironmentService;
    this.answerService = answerService;
    this.enrolleeService = enrolleeService;
    this.metricsDao = metricsDao;
  }

  public List<SurveyAnswerDatum> loadMetricsByField(
      AdminUser user,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      String surveyStableId,
      String questionStableId) {
    authUtilService.authUserToPortal(user, portalShortcode);
    authUtilService.authUserToStudy(user, portalShortcode, studyShortcode);
    StudyEnvironment studyEnv =
        studyEnvironmentService.findByStudy(studyShortcode, environmentName).get(); // TODO
    List<Enrollee> enrollees =
        enrolleeService.findByStudyEnvironment(studyEnv.getId(), true, "created_at", "DESC");
    List<UUID> enrolleeIds = enrollees.stream().map(Enrollee::getId).toList();

    List<Answer> answers =
        answerService.findAllByEnrolleeIdsAndQuestionStableId(enrolleeIds, questionStableId);

    return answers.stream()
        .map(
            answer -> {
              SurveyAnswerDatum datum =
                  SurveyAnswerDatum.builder()
                      .booleanValue(answer.getBooleanValue())
                      .numberValue(answer.getNumberValue())
                      .stringValue(answer.getStringValue())
                      .time(answer.getCreatedAt())
                      .objectValue(answer.getObjectValue())
                      .build();

              return datum;
            })
        .toList();
  }

  public List<String> listMetricFields(
      AdminUser user,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      String surveyStableId) {
    authUtilService.authUserToStudy(user, portalShortcode, studyShortcode);

    // TODO: actually scope this to the environment
    return answerService.findDistinctQuestionStableIdsBySurvey(surveyStableId);
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
