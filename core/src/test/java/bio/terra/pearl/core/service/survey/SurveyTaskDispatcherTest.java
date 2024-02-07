package bio.terra.pearl.core.service.survey;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.ParticipantTaskFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.workflow.ParticipantTaskAssignDto;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

class SurveyTaskDispatcherTest extends BaseSpringBootTest {
    @Autowired
    private SurveyFactory surveyFactory;
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    private ParticipantTaskFactory participantTaskFactory;
    @Autowired
    private ParticipantTaskService participantTaskService;
    @Autowired
    private SurveyTaskDispatcher surveyTaskDispatcher;
    @Autowired
    private AdminUserFactory adminUserFactory;

    @Test
    void testIsDuplicateTask() {
        Survey survey = Survey.builder().recur(false).build();
        StudyEnvironmentSurvey studyEnvironmentSurvey = StudyEnvironmentSurvey.builder()
                .survey(survey)
                .build();
        ParticipantTask surveyTask1 = ParticipantTask.builder()
                .targetStableId("TASK_1")
                .taskType(TaskType.SURVEY)
                .build();
        ParticipantTask surveyTask2 = ParticipantTask.builder()
                .targetStableId("TASK_2")
                .taskType(TaskType.SURVEY)
                .build();
        ParticipantTask kitTask = ParticipantTask.builder()
                .taskType(TaskType.KIT_REQUEST)
                .build();
        List<ParticipantTask> existingTasks = List.of(surveyTask1, surveyTask2, kitTask);
        boolean isDuplicate = SurveyTaskDispatcher.isDuplicateTask(studyEnvironmentSurvey, surveyTask1,
                existingTasks);
        assertTrue(isDuplicate);

        ParticipantTask surveyTask3 = ParticipantTask.builder()
                .targetStableId("TASK_3")
                .taskType(TaskType.SURVEY)
                .build();
        isDuplicate = SurveyTaskDispatcher.isDuplicateTask(studyEnvironmentSurvey, surveyTask3,
                existingTasks);
        assertFalse(isDuplicate);
    }

    @Test
    void testOutreachIsDuplicate() {
        Survey survey = Survey.builder().recur(false).build();
        StudyEnvironmentSurvey studyEnvironmentSurvey = StudyEnvironmentSurvey.builder()
                .survey(survey)
                .build();
        ParticipantTask outreachTask1 = ParticipantTask.builder()
                .targetStableId("oh_outsideAdvert")
                .taskType(TaskType.OUTREACH)
                .build();
        List<ParticipantTask> existingTasks = List.of(outreachTask1);
        ParticipantTask outreachTask2 = ParticipantTask.builder()
                .targetStableId("oh_outsideAdvert")
                .taskType(TaskType.OUTREACH)
                .build();
        boolean isDuplicate = SurveyTaskDispatcher.isDuplicateTask(studyEnvironmentSurvey, outreachTask2,
                existingTasks);
        assertTrue(isDuplicate);
    }

    @Test
    @Transactional
    public void testAssign(TestInfo testInfo) {
        StudyEnvironmentFactory.StudyEnvironmentBundle sandboxBundle = studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);
        AdminUser operator = adminUserFactory.buildPersisted(getTestName(testInfo), true);
        EnrolleeFactory.EnrolleeBundle sandbox1 = enrolleeFactory.buildWithPortalUser(getTestName(testInfo), sandboxBundle.getPortalEnv(), sandboxBundle.getStudyEnv());
        EnrolleeFactory.EnrolleeBundle sandbox2 = enrolleeFactory.buildWithPortalUser(getTestName(testInfo), sandboxBundle.getPortalEnv(), sandboxBundle.getStudyEnv());
        Survey survey = surveyFactory.buildPersisted(getTestName(testInfo));
        surveyFactory.attachToEnv(survey, sandboxBundle.getStudyEnv().getId(), true);
        ParticipantTaskAssignDto assignDto = new ParticipantTaskAssignDto(TaskType.SURVEY, survey.getStableId(), survey.getVersion(), null, true, true );
        surveyTaskDispatcher.assign(assignDto, sandboxBundle.getStudyEnv().getId(), operator, null);
        List<ParticipantTask> participantTasks = participantTaskService.findTasksByStudyAndTarget(sandboxBundle.getStudyEnv().getId(), List.of(survey.getStableId()));
        assertThat(participantTasks, hasSize(2));
    }

    @Test
    @Transactional
    public void testAssignDoesntDuplicate(TestInfo testInfo) {
        StudyEnvironmentFactory.StudyEnvironmentBundle sandboxBundle = studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);
        AdminUser operator = adminUserFactory.buildPersisted(getTestName(testInfo), true);
        EnrolleeFactory.EnrolleeBundle sandbox1 = enrolleeFactory.buildWithPortalUser(getTestName(testInfo), sandboxBundle.getPortalEnv(), sandboxBundle.getStudyEnv());
        Survey survey = surveyFactory.buildPersisted(getTestName(testInfo));
        surveyFactory.attachToEnv(survey, sandboxBundle.getStudyEnv().getId(), true);
        // the enrollee already has a task
        participantTaskFactory.buildPersisted(sandbox1, ParticipantTaskFactory.DEFAULT_BUILDER
                .targetStableId(survey.getStableId()).targetAssignedVersion(survey.getVersion()));

        // shouldn't create a duplicate task if directed to assign to all unassigned
        ParticipantTaskAssignDto assignDto = new ParticipantTaskAssignDto(TaskType.SURVEY, survey.getStableId(), survey.getVersion(), null, true, false );
        surveyTaskDispatcher.assign(assignDto, sandboxBundle.getStudyEnv().getId(), operator, null);
        List<ParticipantTask> participantTasks = participantTaskService.findTasksByStudyAndTarget(sandboxBundle.getStudyEnv().getId(), List.of(survey.getStableId()));
        assertThat(participantTasks, hasSize(1));

        // shouldn't create a duplicate task even if the enrolleeId is provided manually, since eligibility override is false
        assignDto = new ParticipantTaskAssignDto(TaskType.SURVEY, survey.getStableId(), survey.getVersion(), List.of(sandbox1.enrollee().getId()), false, false );
        surveyTaskDispatcher.assign(assignDto, sandboxBundle.getStudyEnv().getId(), operator, null);
        participantTasks = participantTaskService.findTasksByStudyAndTarget(sandboxBundle.getStudyEnv().getId(), List.of(survey.getStableId()));
        assertThat(participantTasks, hasSize(1));

        // if overriding eligibility is specified, then a duplicate task should be created
        assignDto = new ParticipantTaskAssignDto(TaskType.SURVEY, survey.getStableId(), survey.getVersion(), List.of(sandbox1.enrollee().getId()), false, true );
        surveyTaskDispatcher.assign(assignDto, sandboxBundle.getStudyEnv().getId(), operator, null);
        participantTasks = participantTaskService.findTasksByStudyAndTarget(sandboxBundle.getStudyEnv().getId(), List.of(survey.getStableId()));
        assertThat(participantTasks, hasSize(2));
    }

}
