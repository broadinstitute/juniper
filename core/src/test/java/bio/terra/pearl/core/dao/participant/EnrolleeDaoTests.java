package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.workflow.ParticipantTaskDao;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.ParticipantTaskFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

public class EnrolleeDaoTests extends BaseSpringBootTest {
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private ParticipantTaskFactory participantTaskFactory;
    @Autowired
    private EnrolleeDao enrolleeDao;

    @Test
    @Transactional
    public void testFindUnassignedToTask(TestInfo testInfo) {
        String TASK_STABLE_ID = "testFindUnassignedToTask";
        // set up a sandbox and irb environment, with 2 enrollees in each
        StudyEnvironmentFactory.StudyEnvironmentBundle sandboxBundle = studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);
        EnrolleeFactory.EnrolleeBundle sandbox1 = enrolleeFactory.buildWithPortalUser(getTestName(testInfo), sandboxBundle.getPortalEnv(), sandboxBundle.getStudyEnv());
        EnrolleeFactory.EnrolleeBundle sandbox2 = enrolleeFactory.buildWithPortalUser(getTestName(testInfo), sandboxBundle.getPortalEnv(), sandboxBundle.getStudyEnv());

        StudyEnvironmentFactory.StudyEnvironmentBundle irbBundle = studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.irb);
        EnrolleeFactory.EnrolleeBundle irb1 = enrolleeFactory.buildWithPortalUser(getTestName(testInfo), irbBundle.getPortalEnv(), irbBundle.getStudyEnv());
        EnrolleeFactory.EnrolleeBundle irb2 = enrolleeFactory.buildWithPortalUser(getTestName(testInfo), irbBundle.getPortalEnv(), irbBundle.getStudyEnv());

        // returns both enrollees if no one is assigned any tasks
        List<Enrollee> unassignedEnrollees = enrolleeDao.findUnassignedToTask(sandboxBundle.getStudyEnv().getId(), TASK_STABLE_ID, null);
        assertThat(unassignedEnrollees, containsInAnyOrder(sandbox1.enrollee(), sandbox2.enrollee()));

        // still returns no enrollees if someone from another environment is assigned
        participantTaskFactory.buildPersisted(irb1, TASK_STABLE_ID, TaskStatus.NEW, TaskType.SURVEY);
        unassignedEnrollees = enrolleeDao.findUnassignedToTask(sandboxBundle.getStudyEnv().getId(), TASK_STABLE_ID, null);
        assertThat(unassignedEnrollees, containsInAnyOrder(sandbox1.enrollee(), sandbox2.enrollee()));

        // returns the unassigned on if the other one is assigned
        participantTaskFactory.buildPersisted(sandbox2, TASK_STABLE_ID, TaskStatus.NEW, TaskType.SURVEY);
        unassignedEnrollees = enrolleeDao.findUnassignedToTask(sandboxBundle.getStudyEnv().getId(), TASK_STABLE_ID, null);
        assertThat(unassignedEnrollees, containsInAnyOrder(sandbox1.enrollee()));

        // returns empty if both are assigned
        participantTaskFactory.buildPersisted(sandbox1, TASK_STABLE_ID, TaskStatus.NEW, TaskType.SURVEY);
        unassignedEnrollees = enrolleeDao.findUnassignedToTask(sandboxBundle.getStudyEnv().getId(), TASK_STABLE_ID, null);
        assertThat(unassignedEnrollees, hasSize(0));

        // returns both for an unrelated task
        unassignedEnrollees = enrolleeDao.findUnassignedToTask(sandboxBundle.getStudyEnv().getId(), "otherTask", null);
        assertThat(unassignedEnrollees, containsInAnyOrder(sandbox1.enrollee(), sandbox2.enrollee()));
    }

    @Test
    @Transactional
    public void testFindUnassignedToTaskByVersion(TestInfo testInfo) {
        String TASK_STABLE_ID = "testFindUnassignedToTaskByVersion";
        // set up a sandbox and irb environment, with 2 enrollees in each
        StudyEnvironmentFactory.StudyEnvironmentBundle sandboxBundle = studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);
        EnrolleeFactory.EnrolleeBundle sandbox1 = enrolleeFactory.buildWithPortalUser(getTestName(testInfo), sandboxBundle.getPortalEnv(), sandboxBundle.getStudyEnv());
        EnrolleeFactory.EnrolleeBundle sandbox2 = enrolleeFactory.buildWithPortalUser(getTestName(testInfo), sandboxBundle.getPortalEnv(), sandboxBundle.getStudyEnv());

        // give the first enrollee v1 of the task, and the second enrollee v2
        participantTaskFactory.buildPersisted(sandbox1, ParticipantTaskFactory.DEFAULT_BUILDER
                .targetStableId(TASK_STABLE_ID)
                .targetAssignedVersion(1));
        participantTaskFactory.buildPersisted(sandbox2, ParticipantTaskFactory.DEFAULT_BUILDER
                .targetStableId(TASK_STABLE_ID)
                .targetAssignedVersion(2));

        // returns neither if null version specified
        List<Enrollee> unassignedEnrollees = enrolleeDao.findUnassignedToTask(sandboxBundle.getStudyEnv().getId(), TASK_STABLE_ID, null);
        assertThat(unassignedEnrollees, hasSize(0));
        // returns that enrollee1 has not been assigned version 2 of the task
        unassignedEnrollees = enrolleeDao.findUnassignedToTask(sandboxBundle.getStudyEnv().getId(), TASK_STABLE_ID, 2);
        assertThat(unassignedEnrollees, containsInAnyOrder(sandbox1.enrollee()));
    }
}
