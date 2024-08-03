package bio.terra.pearl.core.dao.metrics;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.PortalParticipantUserFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.metrics.BasicMetricDatum;
import bio.terra.pearl.core.model.metrics.TimeRange;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.survey.SurveyTaskDispatcher;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class MetricsDaoTest extends BaseSpringBootTest {

  @Test
  @Transactional
  public void testStudyEnrollmentMetrics(TestInfo info) {
    StudyEnvironment studyEnvironment = studyEnvironmentFactory.buildPersisted(getTestName(info));
    enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
      BaseEntity.BaseEntityBuilder builder = enrolleeFactory.builderWithDependencies(getTestName(info), studyEnvironment)
        .createdAt(Instant.now().minus(Duration.ofDays(4)));
    // The superBuilder 'createdAt' method returns the type of builder the property is contained in, so we have to recast
    enrolleeFactory.buildPersisted((Enrollee.EnrolleeBuilder) builder);
    List<BasicMetricDatum> metrics = metricsDao.studyEnrollments(studyEnvironment.getId(), new TimeRange(null, null));
    assertThat(metrics, hasSize(2));

      Instant oneDayAgo = Instant.now().minus(Duration.ofDays(1));

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
  public void testStudyConsentedMetric(TestInfo info) {
    StudyEnvironment studyEnvironment = studyEnvironmentFactory.buildPersisted(getTestName(info));
    enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
      Enrollee.EnrolleeBuilder builder = enrolleeFactory.builderWithDependencies(getTestName(info), studyEnvironment)
        .consented(true);
    enrolleeFactory.buildPersisted((Enrollee.EnrolleeBuilder) builder);
      List<BasicMetricDatum> rangeMetrics = metricsDao.studyConsentedEnrollees(studyEnvironment.getId(),
        new TimeRange(null, null));
    assertThat(rangeMetrics, hasSize(1));
  }

  /** Tests counting of both required and all completion metrics */
  @Test
  @Transactional
  public void testStudySurveyMetric(TestInfo info) {
    PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
    StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));

    Survey survey = surveyFactory.buildPersisted(surveyFactory.builderWithDependencies(getTestName(info)).required(true));
    StudyEnvironmentSurvey studyEnvSurvey = StudyEnvironmentSurvey.builder().surveyId(survey.getId()).studyEnvironmentId(studyEnv.getId())
        .survey(survey).build();
    studyEnvironmentSurveyService.create(studyEnvSurvey);

    Survey optionalSurvey = surveyFactory.buildPersisted(getTestName(info));
    StudyEnvironmentSurvey optStudyEnvSurvey = StudyEnvironmentSurvey.builder()
        .survey(optionalSurvey).surveyId(optionalSurvey.getId()).studyEnvironmentId(studyEnv.getId()).build();
    studyEnvironmentSurveyService.create(optStudyEnvSurvey);

    Enrollee enrollee1 = enrolleeFactory.buildPersisted(getTestName(info), studyEnv);
      PortalParticipantUser ppUser1 = portalParticipantUserFactory.buildPersisted(getTestName(info), enrollee1, portalEnv);
    Enrollee enrollee2 = enrolleeFactory.buildPersisted(getTestName(info), studyEnv);
      PortalParticipantUser ppUser2 = portalParticipantUserFactory.buildPersisted(getTestName(info), enrollee2, portalEnv);
    Enrollee enrollee3 = enrolleeFactory.buildPersisted(getTestName(info), studyEnv);
      PortalParticipantUser ppUser3 = portalParticipantUserFactory.buildPersisted(getTestName(info), enrollee3, portalEnv);
      ParticipantTask task = surveyTaskDispatcher.buildTask( enrollee1, ppUser1, studyEnvSurvey, studyEnvSurvey.getSurvey());
    task.setStatus(TaskStatus.COMPLETE);
      DataAuditInfo auditInfo = getAuditInfo(info);
    participantTaskService.create(task, auditInfo);
    participantTaskService.create(surveyTaskDispatcher.buildTask(enrollee2, ppUser2, studyEnvSurvey, studyEnvSurvey.getSurvey()), auditInfo);
    ParticipantTask taskToUpdate = participantTaskService.create(surveyTaskDispatcher.buildTask(enrollee3, ppUser3, studyEnvSurvey, studyEnvSurvey.getSurvey()), auditInfo);

      List<BasicMetricDatum> rangeMetrics = metricsDao.studyRequiredSurveyCompletions(studyEnv.getId(), new TimeRange(null, null));
    assertThat(rangeMetrics, hasSize(1));
    taskToUpdate.setStatus(TaskStatus.COMPLETE);
    participantTaskService.update(taskToUpdate, auditInfo);
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
