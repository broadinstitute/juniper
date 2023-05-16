package bio.terra.pearl.core.dao.metrics;

import bio.terra.pearl.core.model.metrics.BasicMetricDatum;
import bio.terra.pearl.core.model.metrics.TimeRange;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.springframework.stereotype.Component;

@Component
public class MetricsDao {
  private Jdbi jdbi;

  public MetricsDao(Jdbi jdbi) {
    this.jdbi = jdbi;
    jdbi.registerRowMapper(BasicMetricDatum.class, BeanMapper.of(BasicMetricDatum.class));
  }

  public List<BasicMetricDatum> studyEnrollments(UUID studyEnvironmentId, TimeRange range) {
    return jdbi.withHandle(handle ->
        handle.createQuery("""
            select 'study_reg' as metric, '' as subcategory, created_at as time
             from enrollee where study_environment_id = :studyEnvironmentId and %s;
             """.formatted(getTimeRangeQueryString("created_at", range)))
            .bind("studyEnvironmentId", studyEnvironmentId)
            .bindBean(range)
            .mapToBean(BasicMetricDatum.class)
            .list()
    );
  }

  public List<BasicMetricDatum> studyConsentedEnrollees(UUID studyEnvironmentId, TimeRange range) {
    return jdbi.withHandle(handle ->
        handle.createQuery("""
            select 'study_consent' as metric, '' as subcategory, created_at as time
             from enrollee where study_environment_id = :studyEnvironmentId and consented = true and %s;
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
            select 'study_survey' as metric, targetStableId as subcategory, completed_at as time
             from participant_task 
             where study_environment_id = :studyEnvironmentId 
             and task_type = 'SURVEY'
             and status = 'COMPLETE'
             and %s;
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
             and %s;
             """.formatted(getTimeRangeQueryString("created_at", range)))
            .bind("studyEnvironmentId", studyEnvironmentId)
            .bindBean(range)
            .mapToBean(BasicMetricDatum.class)
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
