package bio.terra.pearl.core.dao.metrics;

import bio.terra.pearl.core.model.metrics.BasicMetricDatum;
import bio.terra.pearl.core.model.metrics.SurveyAnswerDatum;
import bio.terra.pearl.core.model.metrics.TimeRange;
import java.util.List;
import java.util.UUID;

import bio.terra.pearl.core.model.survey.Answer;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.springframework.stereotype.Component;

/**
 * Returns lists of BasicMetricDatum for metrics queries
 * The returned data from each query should be sorted ascending by the 'time' property of each datum.
 * Because metrics are read-only -- there is only a DAO -- no need for a service yet
 * */
@Component
public class MetricsDao {
  private Jdbi jdbi;

  public MetricsDao(Jdbi jdbi) {
    this.jdbi = jdbi;
    jdbi.registerRowMapper(BasicMetricDatum.class, BeanMapper.of(BasicMetricDatum.class));
    jdbi.registerRowMapper(Answer.class, BeanMapper.of(Answer.class));
  }

  public List<BasicMetricDatum> studyEnrollments(UUID studyEnvironmentId, TimeRange range) {
    return jdbi.withHandle(handle ->
        handle.createQuery("""
            select 'study_reg' as metric, '' as subcategory, created_at as time
             from enrollee 
             where study_environment_id = :studyEnvironmentId 
             and %s
             order by created_at asc;
             """.formatted(getTimeRangeQueryString("created_at", range)))
            .bind("studyEnvironmentId", studyEnvironmentId)
            .bindBean(range)
            .mapToBean(BasicMetricDatum.class)
            .list()
    );
  }

  /** For now, the time returned is the time the enrollee was created (registration).  Eventually we'll want to
   * return time-of-consent (which is complex, because consent may involve multiple tasks) */
  public List<BasicMetricDatum> studyConsentedEnrollees(UUID studyEnvironmentId, TimeRange range) {
    return jdbi.withHandle(handle ->
        handle.createQuery("""
            select 'study_consent' as metric, '' as subcategory, created_at as time
             from enrollee 
             where study_environment_id = :studyEnvironmentId 
             and consented = true and %s
             order by created_at asc;
             """.formatted(getTimeRangeQueryString("created_at", range)))
            .bind("studyEnvironmentId", studyEnvironmentId)
            .bindBean(range)
            .mapToBean(BasicMetricDatum.class)
            .list()
    );
  }

  public List<BasicMetricDatum> studySurveyCompletions(UUID studyEnvironmentId, TimeRange range) {
    return jdbi.withHandle(handle ->
        handle.createQuery("""
            select 'study_survey' as metric, target_stable_id as subcategory, completed_at as time
             from participant_task 
             where study_environment_id = :studyEnvironmentId 
             and task_type = 'SURVEY'
             and status = 'COMPLETE'
             and %s
             order by completed_at asc;
             """.formatted(getTimeRangeQueryString("created_at", range)))
            .bind("studyEnvironmentId", studyEnvironmentId)
            .bindBean(range)
            .mapToBean(BasicMetricDatum.class)
            .list()
    );
  }

  public List<BasicMetricDatum> studyRequiredSurveyCompletions(UUID studyEnvironmentId, TimeRange range) {
    return jdbi.withHandle(handle ->
        handle.createQuery("""
            select 'study_required_survey' as metric, target_stable_id as subcategory, completed_at as time
             from participant_task 
             where study_environment_id = :studyEnvironmentId 
             and task_type = 'SURVEY'
             and status = 'COMPLETE'
             and blocks_hub = true 
             and %s
             order by completed_at asc;
             """.formatted(getTimeRangeQueryString("created_at", range)))
            .bind("studyEnvironmentId", studyEnvironmentId)
            .bindBean(range)
            .mapToBean(BasicMetricDatum.class)
            .list()
    );
  }

  public List<Answer> surveyQuestionResponses(String surveyStableId, String questionStableId, TimeRange range) {
    return jdbi.withHandle(handle ->
        handle.createQuery("""
WITH ranked_answers AS (
  SELECT
    id,
    enrollee_id,
    string_value,
    boolean_value,
    number_value,
    object_value,
    survey_response_id,
    question_stable_id,
    survey_version,
    ROW_NUMBER() OVER (PARTITION BY enrollee_id ORDER BY survey_version DESC) AS row_num
  FROM
    answer
  WHERE
    survey_stable_id = :surveyStableId
    and question_stable_id = :questionStableId
)
SELECT
  id,
  enrollee_id,
  string_value,
  boolean_value,
  number_value,
  object_value,
  survey_response_id,
  question_stable_id,
  survey_version
FROM
  ranked_answers
WHERE
  row_num = 1;""")
            .bind("surveyStableId", surveyStableId)
            .bind("questionStableId", questionStableId)
            .mapToBean(Answer.class)
            .list()
    );
  }

  protected String getTimeRangeQueryString(String timeField, TimeRange timeRange) {
    if (timeRange.getStart() == null && timeRange.getEnd() == null) {
      return " 1 = 1 ";
    }
    if (timeRange.getStart() == null) {
      return " %s < :end ".formatted(timeField);
    }
    if (timeRange.getEnd() == null) {
      return " %s >= :start ".formatted(timeField);
    }
    return " %s >= :start and %s < :end ".formatted(timeField, timeField);
  }

}
