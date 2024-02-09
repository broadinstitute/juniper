package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.ParticipantTaskFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ParticipantTaskServiceTests extends BaseSpringBootTest {
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired private EnrolleeFactory enrolleeFactory;
    @Autowired private ParticipantTaskFactory participantTaskFactory;
    @Autowired private ParticipantTaskService participantTaskService;

    @Test
    @Transactional
    public void testUpdateTasksForSurvey(TestInfo info) {
        StudyEnvironmentFactory.StudyEnvironmentBundle bundle =
                studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        EnrolleeFactory.EnrolleeBundle enrollee1 =
                enrolleeFactory.buildWithPortalUser(
                        getTestName(info), bundle.getPortalEnv(), bundle.getStudyEnv());
        EnrolleeFactory.EnrolleeBundle enrollee2 =
                enrolleeFactory.buildWithPortalUser(
                        getTestName(info), bundle.getPortalEnv(), bundle.getStudyEnv());

        ParticipantTask task1 =
                participantTaskFactory.buildPersisted(
                        enrollee1,
                        ParticipantTaskFactory.DEFAULT_BUILDER
                                .targetStableId("surveyA")
                                .targetAssignedVersion(1));
        ParticipantTask task2 =
                participantTaskFactory.buildPersisted(
                        enrollee2,
                        ParticipantTaskFactory.DEFAULT_BUILDER
                                .targetStableId("surveyA")
                                .targetAssignedVersion(2));
        ParticipantTask differentSurveyTask =
                participantTaskFactory.buildPersisted(
                        enrollee2,
                        ParticipantTaskFactory.DEFAULT_BUILDER
                                .targetStableId("surveyB")
                                .targetAssignedVersion(1));

        // issue a command to update surveyA tasks to version 2
        ParticipantTaskUpdateDto.TaskUpdateSpec updateSpec =
                new ParticipantTaskUpdateDto.TaskUpdateSpec("surveyA", 2, 1, null);
        ParticipantTaskUpdateDto updateDto =
                new ParticipantTaskUpdateDto(List.of(updateSpec), null, true);
        participantTaskService.updateTasks(
                bundle.getStudyEnv().getId(),
                updateDto,
                new ResponsibleEntity(getTestName(info)));

        // check that the task for the specified survey (surveyA) got updated, but that the surveyB task
        // did not
        ParticipantTask updatedTask = participantTaskService.find(task1.getId()).orElseThrow();
        assertThat(updatedTask.getTargetAssignedVersion(), equalTo(2));
        ParticipantTask unaffectedTaskUpdate =
                participantTaskService.find(differentSurveyTask.getId()).orElseThrow();
        assertThat(unaffectedTaskUpdate.getTargetAssignedVersion(), equalTo(1));
    }
}
