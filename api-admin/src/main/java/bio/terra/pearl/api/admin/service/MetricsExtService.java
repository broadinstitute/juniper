package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.core.dao.metrics.MetricName;
import bio.terra.pearl.core.dao.metrics.MetricsDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.metrics.BasicMetricDatum;
import bio.terra.pearl.core.model.metrics.SurveyAnswerDatum;
import bio.terra.pearl.core.model.metrics.TimeRange;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.survey.AnswerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class MetricsExtService {
  private AuthUtilService authUtilService;
  private StudyEnvironmentService studyEnvironmentService;
  private AnswerService answerService;
  private MetricsDao metricsDao;

  public MetricsExtService(
      AuthUtilService authUtilService,
      StudyEnvironmentService studyEnvironmentService,
      AnswerService answerService,
      MetricsDao metricsDao) {
    this.authUtilService = authUtilService;
    this.studyEnvironmentService = studyEnvironmentService;
    this.answerService = answerService;
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

  public List<SurveyAnswerDatum> loadMetricsByField(
      AdminUser user,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      String surveyStableId,
      String questionStableId) {
    authUtilService.authUserToStudy(user, portalShortcode, studyShortcode);
    List<Answer> answers =
        metricsDao.surveyQuestionResponses(
            surveyStableId, questionStableId, new TimeRange(null, null));

    return answers.stream()
        .flatMap(
            answer -> {
              if (answer.getObjectValue() != null) {
                // Assuming the objectValue is a List<String>
                ObjectMapper objectMapper = new ObjectMapper();
                List<String> stringValues;
                try {
                  stringValues =
                      objectMapper.readValue(
                          (String) answer.getObjectValue(), new TypeReference<List<String>>() {});
                } catch (JsonProcessingException e) {
                  throw new RuntimeException(e);
                }
                return stringValues.stream()
                    .map(
                        stringValue -> {
                          SurveyAnswerDatum datum =
                              SurveyAnswerDatum.builder()
                                  .booleanValue(answer.getBooleanValue())
                                  .numberValue(answer.getNumberValue())
                                  .stringValue(stringValue)
                                  .time(answer.getCreatedAt())
                                  .build();
                          return datum;
                        });
              } else {
                SurveyAnswerDatum datum =
                    SurveyAnswerDatum.builder()
                        .booleanValue(answer.getBooleanValue())
                        .numberValue(answer.getNumberValue())
                        .stringValue(answer.getStringValue())
                        .time(answer.getCreatedAt())
                        .objectValue(answer.getObjectValue())
                        .build();
                return Stream.of(datum);
              }
            })
        .collect(Collectors.toList());
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
}
