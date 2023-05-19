package bio.terra.pearl.core.dao.metrics;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.PortalParticipantUserFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.metrics.BasicMetricDatum;
import bio.terra.pearl.core.model.metrics.TimeRange;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.service.participant.ParticipantTaskService;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.survey.SurveyTaskDispatcher;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class MetricsDaoTest extends BaseSpringBootTest {

  @Test
  @Transactional
  public void testStudyEnrollmentMetrics() {
    StudyEnvironment studyEnvironment = studyEnvironmentFactory.buildPersisted("testStudyEnrollmentMetrics");
    enrolleeFactory.buildPersisted("testStudyEnrollmentMetrics", studyEnvironment);
    var builder = enrolleeFactory.builderWithDependencies("testStudyEnrollmentMetrics", studyEnvironment)
        .createdAt(Instant.now().minus(Duration.ofDays(4)));
    // The superBuilder 'createdAt' method returns the type of builder the property is contained in, so we have to recast
    enrolleeFactory.buildPersisted((Enrollee.EnrolleeBuilder) builder);
    List<BasicMetricDatum> metrics = metricsDao.studyEnrollments(studyEnvironment.getId(), new TimeRange(null, null));
    assertThat(metrics, hasSize(2));

    var oneDayAgo = Instant.now().minus(Duration.ofDays(1));

    List<BasicMetricDatum> rangeMetrics = metricsDao.studyEnrollments(studyEnvironment.getId(),
        new TimeRange(oneDayAgo, null));
    assertThat(rangeMetrics, hasSize(1));
    assertThat(rangeMetrics.get(0).getTime().isAfter(oneDayAgo), equalTo(true));

    rangeMetrics = metricsDao.studyEnrollments(studyEnvironment.getId(),
        new TimeRange(null, Instant.now().minus(Duration.ofDays(1))));
    assertThat(rangeMetrics, hasSize(1));
    assertThat(rangeMetrics.get(0).getTime().isBefore(oneDayAgo), equalTo(true));

    // nothing should return from earlier than 6 days ago
    rangeMetrics = metricsDao.studyEnrollments(studyEnvironment.getId(),
        new TimeRange(null, Instant.now().minus(Duration.ofDays(6))));
    assertThat(rangeMetrics, hasSize(0));
  }

  @Test
  @Transactional
  public void testStudyConsentedMetric() {
    StudyEnvironment studyEnvironment = studyEnvironmentFactory.buildPersisted("testStudyEnrollmentMetrics");
    enrolleeFactory.buildPersisted("testStudyEnrollmentMetrics", studyEnvironment);
    var builder = enrolleeFactory.builderWithDependencies("testStudyEnrollmentMetrics", studyEnvironment)
        .consented(true);
    enrolleeFactory.buildPersisted((Enrollee.EnrolleeBuilder) builder);
    var rangeMetrics = metricsDao.studyConsentedEnrollees(studyEnvironment.getId(),
        new TimeRange(null, null));
    assertThat(rangeMetrics, hasSize(1));
  }

  /** Tests counting of both required and all completion metrics */
  @Test
  @Transactional
  public void testStudySurveyMetric() {
    PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted("testEnrollAndConsent");
    StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, "testEnrollAndConsent");

    Survey survey = surveyFactory.buildPersisted("testStudySurveyMetric");
    StudyEnvironmentSurvey studyEnvSurvey = StudyEnvironmentSurvey.builder().surveyId(survey.getId()).studyEnvironmentId(studyEnv.getId())
        .survey(survey)
        .required(true).build();
    studyEnvironmentSurveyService.create(studyEnvSurvey);

    Survey optionalSurvey = surveyFactory.buildPersisted("testStudySurveyMetric");
    StudyEnvironmentSurvey optStudyEnvSurvey = StudyEnvironmentSurvey.builder()
        .survey(optionalSurvey).surveyId(survey.getId()).studyEnvironmentId(studyEnv.getId()).build();
    studyEnvironmentSurveyService.create(optStudyEnvSurvey);

    Enrollee enrollee1 = enrolleeFactory.buildPersisted("testStudySurveyMetric", studyEnv);
    var ppUser1 = portalParticipantUserFactory.buildPersisted("testStudySurveyMetric", enrollee1, portalEnv);
    Enrollee enrollee2 = enrolleeFactory.buildPersisted("testStudySurveyMetric", studyEnv);
    var ppUser2 = portalParticipantUserFactory.buildPersisted("testStudySurveyMetric", enrollee2, portalEnv);
    Enrollee enrollee3 = enrolleeFactory.buildPersisted("testStudySurveyMetric", studyEnv);
    var ppUser3 = portalParticipantUserFactory.buildPersisted("testStudySurveyMetric", enrollee3, portalEnv);

    var task = surveyTaskDispatcher.buildTask(studyEnvSurvey, enrollee1, ppUser1);
    task.setStatus(TaskStatus.COMPLETE);
    participantTaskService.create(task);
    participantTaskService.create(surveyTaskDispatcher.buildTask(studyEnvSurvey, enrollee2, ppUser2));
    var taskToUpdate = participantTaskService.create(surveyTaskDispatcher.buildTask(studyEnvSurvey, enrollee3, ppUser3));

    var rangeMetrics = metricsDao.studyRequiredSurveyCompletions(studyEnv.getId(), new TimeRange(null, null));
    assertThat(rangeMetrics, hasSize(1));
    taskToUpdate.setStatus(TaskStatus.COMPLETE);
    participantTaskService.update(taskToUpdate);
    rangeMetrics = metricsDao.studyRequiredSurveyCompletions(studyEnv.getId(), new TimeRange(null, null));
    assertThat(rangeMetrics, hasSize(2));
  }

  @Autowired
  private MetricsDao metricsDao;

  @Autowired
  private EnrolleeFactory enrolleeFactory;
  @Autowired
  private StudyEnvironmentFactory studyEnvironmentFactory;
  @Autowired
  private SurveyFactory surveyFactory;
  @Autowired
  private StudyEnvironmentSurveyService studyEnvironmentSurveyService;
  @Autowired
  private SurveyTaskDispatcher surveyTaskDispatcher;

  @Autowired
  private PortalEnvironmentFactory portalEnvironmentFactory;
  @Autowired
  private PortalParticipantUserFactory portalParticipantUserFactory;
  @Autowired
  private ParticipantTaskService participantTaskService;
}
