package bio.terra.pearl.api.participant.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import bio.terra.pearl.api.participant.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentBundle;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.ParticipantTaskFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.model.participant.RelationshipType;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.participant.EnrolleeRelationService;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class ParticipantTaskExtServiceTests extends BaseSpringBootTest {
  @Autowired private StudyEnvironmentFactory studyEnvironmentFactory;
  @Autowired private EnrolleeFactory enrolleeFactory;
  @Autowired private SurveyFactory surveyFactory;
  @Autowired private ParticipantTaskFactory participantTaskFactory;
  @Autowired private ParticipantTaskExtService participantTaskExtService;
  @Autowired private EnrolleeRelationService enrolleeRelationService;

  @Test
  @Transactional
  public void testListSurveyTasks(TestInfo info) {
    StudyEnvironmentBundle bundle =
        studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
    EnrolleeBundle enrollee1 =
        enrolleeFactory.buildWithPortalUser(
            getTestName(info), bundle.getPortalEnv(), bundle.getStudyEnv());
    EnrolleeBundle enrollee2 =
        enrolleeFactory.buildWithPortalUser(
            getTestName(info), bundle.getPortalEnv(), bundle.getStudyEnv());
    Survey survey = surveyFactory.buildPersisted(getTestName(info), bundle.getPortal().getId());
    ParticipantTask task1 =
        participantTaskFactory.buildPersisted(
            enrollee1,
            ParticipantTaskFactory.DEFAULT_BUILDER
                .targetStableId(survey.getStableId())
                .targetAssignedVersion(survey.getVersion())
                .taskType(TaskType.OUTREACH));
    ParticipantTask task2 =
        participantTaskFactory.buildPersisted(
            enrollee2,
            ParticipantTaskFactory.DEFAULT_BUILDER
                .targetStableId(survey.getStableId())
                .targetAssignedVersion(survey.getVersion())
                .taskType(TaskType.OUTREACH));

    // enrollee can fetch their own tasks
    List<ParticipantTaskExtService.TaskAndSurvey> tasks =
        participantTaskExtService.listSurveyTasks(
            enrollee1.participantUser(),
            bundle.getPortal().getShortcode(),
            EnvironmentName.sandbox,
            TaskType.OUTREACH,
            enrollee1.participantUser().getId());
    assertThat(tasks, hasSize(1));
    assertThat(tasks.get(0).task().getId(), equalTo(task1.getId()));

    // enrollee can't fetch other enrollee's tasks
    Assertions.assertThrows(
        NotFoundException.class,
        () -> {
          participantTaskExtService.listSurveyTasks(
              enrollee1.participantUser(),
              bundle.getPortal().getShortcode(),
              EnvironmentName.sandbox,
              TaskType.OUTREACH,
              enrollee2.participantUser().getId());
        });

    // enrollee can fetch a proxy's tasks
    enrolleeRelationService.create(
        EnrolleeRelation.builder()
            .enrolleeId(enrollee1.enrollee().getId())
            .targetEnrolleeId(enrollee2.enrollee().getId())
            .relationshipType(RelationshipType.PROXY)
            .build(),
        DataAuditInfo.builder().systemProcess("test").build());
    tasks =
        participantTaskExtService.listSurveyTasks(
            enrollee1.participantUser(),
            bundle.getPortal().getShortcode(),
            EnvironmentName.sandbox,
            TaskType.OUTREACH,
            enrollee2.participantUser().getId());
    assertThat(tasks, hasSize(1));
    assertThat(tasks.get(0).task().getId(), equalTo(task2.getId()));
  }
}
