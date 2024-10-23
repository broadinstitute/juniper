package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.dataimport.TimeShiftDao;
import bio.terra.pearl.core.factory.StudyEnvironmentBundle;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeAndProxy;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.ParticipantTaskFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.factory.survey.SurveyResponseFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.survey.RecurrenceType;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    @Autowired
    private SurveyResponseFactory surveyResponseFactory;
    @Autowired
    private TimeShiftDao timeShiftDao;


    @Test
    void testIsDuplicateTask() {
        Survey survey = Survey.builder().recurrenceType(RecurrenceType.NONE).build();
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
    void testIsDuplicateForTaskTypes() {
        Survey survey = Survey.builder().recurrenceType(RecurrenceType.NONE).build();
        StudyEnvironmentSurvey studyEnvironmentSurvey = StudyEnvironmentSurvey.builder()
                .survey(survey)
                .build();
        List.of(TaskType.SURVEY, TaskType.CONSENT, TaskType.OUTREACH, TaskType.ADMIN_FORM).stream().forEach(taskType -> {
            ParticipantTask surveyTask1 = ParticipantTask.builder()
                    .targetStableId("TASK_1")
                    .taskType(taskType)
                    .build();
            ParticipantTask surveyTask2 = ParticipantTask.builder()
                    .targetStableId("TASK_1")
                    .taskType(taskType)
                    .build();
            boolean isDuplicate = SurveyTaskDispatcher.isDuplicateTask(studyEnvironmentSurvey, surveyTask2,
                    List.of(surveyTask1));
            assertTrue(isDuplicate);
        });
    }

    @Test
    @Transactional
    public void testAutoAssign(TestInfo testInfo) {
        StudyEnvironmentBundle sandboxBundle = studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);
        Survey survey = surveyFactory.buildPersisted(surveyFactory.builderWithDependencies(getTestName(testInfo))
                .stableId("main")
                .content("{\"pages\":[{\"elements\":[{\"type\":\"text\",\"name\":\"diagnosis\",\"title\":\"What is your diagnosis?\"}]}]}")
                        .portalId(sandboxBundle.getPortal().getId())
                .autoAssign(true));
        surveyFactory.attachToEnv(survey, sandboxBundle.getStudyEnv().getId(), true);
        Survey followUpSurvey = surveyFactory.buildPersisted(surveyFactory.builderWithDependencies(getTestName(testInfo))
                .stableId("followUp")
                .portalId(sandboxBundle.getPortal().getId())
                .autoAssign(false));
        surveyFactory.attachToEnv(followUpSurvey, sandboxBundle.getStudyEnv().getId(), true);

        EnrolleeBundle sandbox1 = enrolleeFactory.enroll(getTestName(testInfo), sandboxBundle.getPortal().getShortcode(), sandboxBundle.getStudy().getShortcode(), sandboxBundle.getPortalEnv().getEnvironmentName());

        // confirm only the main survey is assigned automatically
        List<ParticipantTask> participantTasks = participantTaskService.findByEnrolleeId(sandbox1.enrollee().getId());
        assertThat(participantTasks, hasSize(1));
        assertThat(participantTasks.get(0).getTargetStableId(), equalTo("main"));

        // confirm that even after a survey submit event with a completion, the followup task is still not assigned
        surveyResponseFactory.submitStringAnswer(participantTasks.get(0), "diagnosis", "sick", true, sandbox1, sandboxBundle.getPortal());
        participantTasks = participantTaskService.findByEnrolleeId(sandbox1.enrollee().getId());
        assertThat(participantTasks, hasSize(1));
        assertThat(participantTasks.get(0).getStatus(), equalTo(TaskStatus.COMPLETE));
    }

    @Test
    @Transactional
    public void testAssign(TestInfo testInfo) {
        StudyEnvironmentBundle sandboxBundle = studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);
        AdminUser operator = adminUserFactory.buildPersisted(getTestName(testInfo), true);
        EnrolleeBundle sandbox1 = enrolleeFactory.buildWithPortalUser(getTestName(testInfo), sandboxBundle.getPortalEnv(), sandboxBundle.getStudyEnv());
        EnrolleeBundle sandbox2 = enrolleeFactory.buildWithPortalUser(getTestName(testInfo), sandboxBundle.getPortalEnv(), sandboxBundle.getStudyEnv());
        Survey survey = surveyFactory.buildPersisted(getTestName(testInfo));
        surveyFactory.attachToEnv(survey, sandboxBundle.getStudyEnv().getId(), true);
        ParticipantTaskAssignDto assignDto = new ParticipantTaskAssignDto(TaskType.SURVEY, survey.getStableId(), survey.getVersion(), null, true, true );
        surveyTaskDispatcher.assign(assignDto, sandboxBundle.getStudyEnv().getId(), new ResponsibleEntity(operator));
        List<ParticipantTask> participantTasks = participantTaskService.findTasksByStudyAndTarget(sandboxBundle.getStudyEnv().getId(), List.of(survey.getStableId()));
        assertThat(participantTasks, hasSize(2));
    }

    @Test
    @Transactional
    public void testAssignDoesntDuplicate(TestInfo testInfo) {
        StudyEnvironmentBundle sandboxBundle = studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);
        AdminUser operator = adminUserFactory.buildPersisted(getTestName(testInfo), true);
        EnrolleeBundle sandbox1 = enrolleeFactory.buildWithPortalUser(getTestName(testInfo), sandboxBundle.getPortalEnv(), sandboxBundle.getStudyEnv());
        Survey survey = surveyFactory.buildPersisted(getTestName(testInfo));
        surveyFactory.attachToEnv(survey, sandboxBundle.getStudyEnv().getId(), true);
        // the enrollee already has a task
        participantTaskFactory.buildPersisted(sandbox1, ParticipantTaskFactory.DEFAULT_BUILDER
                .targetStableId(survey.getStableId()).targetAssignedVersion(survey.getVersion()));

        // shouldn't create a duplicate task if directed to assign to all unassigned
        ParticipantTaskAssignDto assignDto = new ParticipantTaskAssignDto(TaskType.SURVEY, survey.getStableId(), survey.getVersion(), null, true, false );
        surveyTaskDispatcher.assign(assignDto, sandboxBundle.getStudyEnv().getId(), new ResponsibleEntity(operator));
        List<ParticipantTask> participantTasks = participantTaskService.findTasksByStudyAndTarget(sandboxBundle.getStudyEnv().getId(), List.of(survey.getStableId()));
        assertThat(participantTasks, hasSize(1));

        // shouldn't create a duplicate task even if the enrolleeId is provided manually, since eligibility override is false
        assignDto = new ParticipantTaskAssignDto(TaskType.SURVEY, survey.getStableId(), survey.getVersion(), List.of(sandbox1.enrollee().getId()), false, false );
        surveyTaskDispatcher.assign(assignDto, sandboxBundle.getStudyEnv().getId(), new ResponsibleEntity(operator));
        participantTasks = participantTaskService.findTasksByStudyAndTarget(sandboxBundle.getStudyEnv().getId(), List.of(survey.getStableId()));
        assertThat(participantTasks, hasSize(1));

        // if overriding eligibility is specified, then a duplicate task should be created
        assignDto = new ParticipantTaskAssignDto(TaskType.SURVEY, survey.getStableId(), survey.getVersion(), List.of(sandbox1.enrollee().getId()), false, true );
        surveyTaskDispatcher.assign(assignDto, sandboxBundle.getStudyEnv().getId(), new ResponsibleEntity(operator));
        participantTasks = participantTaskService.findTasksByStudyAndTarget(sandboxBundle.getStudyEnv().getId(), List.of(survey.getStableId()));
        assertThat(participantTasks, hasSize(2));
    }

    @Test
    @Transactional
    public void testAssignWithSearchExpression(TestInfo testInfo) {
        StudyEnvironmentBundle sandboxBundle = studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);
        AdminUser operator = adminUserFactory.buildPersisted(getTestName(testInfo), true);
        Survey survey = surveyFactory.buildPersisted(surveyFactory.builderWithDependencies(getTestName(testInfo))
                .eligibilityRule("{profile.givenName} = 'John'"));
        surveyFactory.attachToEnv(survey, sandboxBundle.getStudyEnv().getId(), true);

        EnrolleeBundle e1 = enrolleeFactory.buildWithPortalUser(getTestName(testInfo), sandboxBundle.getPortalEnv(), sandboxBundle.getStudyEnv(), Profile.builder().givenName("John").familyName("Doe").build());
        enrolleeFactory.buildWithPortalUser(getTestName(testInfo), sandboxBundle.getPortalEnv(), sandboxBundle.getStudyEnv());
        enrolleeFactory.buildWithPortalUser(getTestName(testInfo), sandboxBundle.getPortalEnv(), sandboxBundle.getStudyEnv());
        EnrolleeBundle e2 = enrolleeFactory.buildWithPortalUser(getTestName(testInfo), sandboxBundle.getPortalEnv(), sandboxBundle.getStudyEnv(), Profile.builder().givenName("John").familyName("Smith").build());

        surveyTaskDispatcher.assign(
                new ParticipantTaskAssignDto(TaskType.SURVEY, survey.getStableId(), survey.getVersion(), null, true, false),
                sandboxBundle.getStudyEnv().getId(), new ResponsibleEntity(operator));

        List<ParticipantTask> participantTasks = participantTaskService.findTasksByStudyAndTarget(sandboxBundle.getStudyEnv().getId(), List.of(survey.getStableId()));
        assertThat(participantTasks, hasSize(2));
        assertTrue(participantTasks.stream().anyMatch(t -> t.getEnrolleeId().equals(e1.enrollee().getId())));
        assertTrue(participantTasks.stream().anyMatch(t -> t.getEnrolleeId().equals(e2.enrollee().getId())));
    }

    @Test
    @Transactional
    public void testDoesNotAssignToProxyByDefault(TestInfo testInfo) {
        StudyEnvironmentBundle sandboxBundle = studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);
        AdminUser operator = adminUserFactory.buildPersisted(getTestName(testInfo), true);
        Survey survey = surveyFactory.buildPersisted(getTestName(testInfo));
        surveyFactory.attachToEnv(survey, sandboxBundle.getStudyEnv().getId(), true);

        EnrolleeAndProxy enrolleeAndProxy = enrolleeFactory.buildProxyAndGovernedEnrollee(getTestName(testInfo), sandboxBundle.getPortalEnv(), sandboxBundle.getStudyEnv());
        EnrolleeBundle normalEnrollee = enrolleeFactory.buildWithPortalUser(getTestName(testInfo), sandboxBundle.getPortalEnv(), sandboxBundle.getStudyEnv());

        surveyTaskDispatcher.assign(
                new ParticipantTaskAssignDto(TaskType.SURVEY, survey.getStableId(), survey.getVersion(), null, true, false),
                sandboxBundle.getStudyEnv().getId(), new ResponsibleEntity(operator));

        List<ParticipantTask> participantTasks = participantTaskService.findTasksByStudyAndTarget(sandboxBundle.getStudyEnv().getId(), List.of(survey.getStableId()));
        assertThat(participantTasks, hasSize(2));
        assertTrue(participantTasks.stream().anyMatch(t -> t.getEnrolleeId().equals(enrolleeAndProxy.governedEnrollee().getId())));
        assertTrue(participantTasks.stream().anyMatch(t -> t.getEnrolleeId().equals(normalEnrollee.enrollee().getId())));
        assertTrue(participantTasks.stream().noneMatch(t -> t.getEnrolleeId().equals(enrolleeAndProxy.proxy().getId())));
    }

    @Test
    @Transactional
    public void testAssignOnSurveyEvent(TestInfo testInfo) {
        StudyEnvironmentBundle sandboxBundle = studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);
        AdminUser operator = adminUserFactory.buildPersisted(getTestName(testInfo), true);
        Survey survey = surveyFactory.buildPersisted(surveyFactory.builderWithDependencies(getTestName(testInfo))
                        .content("{\"pages\":[{\"elements\":[{\"type\":\"text\",\"name\":\"diagnosis\",\"title\":\"What is your diagnosis?\"}]}]}")
                .stableId("medForm")
                .portalId(sandboxBundle.getPortal().getId()));    ;
        surveyFactory.attachToEnv(survey, sandboxBundle.getStudyEnv().getId(), true);

        Survey followUpSurvey = surveyFactory.buildPersisted(surveyFactory.builderWithDependencies(getTestName(testInfo))
                        .stableId("followUp")
                .eligibilityRule("{answer.medForm.diagnosis} = 'sick'"));
        surveyFactory.attachToEnv(followUpSurvey, sandboxBundle.getStudyEnv().getId(), true);

        EnrolleeBundle bundle = enrolleeFactory.enroll("healthy@test.com", sandboxBundle.getPortal().getShortcode(), sandboxBundle.getStudy().getShortcode(), sandboxBundle.getPortalEnv().getEnvironmentName());

        List<ParticipantTask> tasks = participantTaskService.findByEnrolleeId(bundle.enrollee().getId());
        // confirm the follow-up survey has not yet been assigned to the participant
        assertThat(tasks, hasSize(1));
        ParticipantTask medFormTask = tasks.get(0);


        surveyResponseFactory.submitStringAnswer(
                medFormTask,
                "diagnosis",
                "sick",
                false,
                bundle,
                sandboxBundle.getPortal());
        // survey not complete, so no new task should be assigned
        assertThat(participantTaskService.findByEnrolleeId(bundle.enrollee().getId()), hasSize(1));

        surveyResponseFactory.submitStringAnswer(
                medFormTask,
                "diagnosis",
                "fine",
                true,
                bundle,
                sandboxBundle.getPortal());
        // survey answer not a match -- no new task should be assigned
        assertThat(participantTaskService.findByEnrolleeId(bundle.enrollee().getId()), hasSize(1));

        surveyResponseFactory.submitStringAnswer(
                medFormTask,
                "diagnosis",
                "sick",
                true,
                bundle,
                sandboxBundle.getPortal());
        // survey answer matches and is complete -- should assign new task
        assertThat(participantTaskService.findByEnrolleeId(bundle.enrollee().getId()), hasSize(2));
        tasks = participantTaskService.findByEnrolleeId(bundle.enrollee().getId());
        // now the task should be added
        assertThat(tasks.stream().map(ParticipantTask::getTargetStableId).toList(), containsInAnyOrder("medForm", "followUp"));
    }

    @Test
    @Transactional
    public void testDelayAssign(TestInfo testInfo) {
        // create a 1-day delayed survey, confirm it doesn't get assigned until the delay has passed
        StudyEnvironmentBundle sandboxBundle = studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);
        Survey survey = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(testInfo))
                        .portalId(sandboxBundle.getPortal().getId())
                .daysAfterEligible(1));
        surveyFactory.attachToEnv(survey, sandboxBundle.getStudyEnv().getId(), true);

        EnrolleeBundle sandbox1 = enrolleeFactory.enroll(getTestName(testInfo) + "1", sandboxBundle.getPortal().getShortcode(), sandboxBundle.getStudy().getShortcode(), EnvironmentName.sandbox);
        EnrolleeBundle sandbox2 = enrolleeFactory.enroll(getTestName(testInfo) + "2", sandboxBundle.getPortal().getShortcode(), sandboxBundle.getStudy().getShortcode(), EnvironmentName.sandbox);

        List<ParticipantTask> tasks = participantTaskService.findByStudyEnvironmentId(sandboxBundle.getStudyEnv().getId());
        assertThat(tasks, hasSize(0));
        timeShiftDao.changeEnrolleeCreationTime(sandbox1.enrollee().getId(), Instant.now().minus(3, ChronoUnit.DAYS));

        // should assign to sandbox1 since it was created more than 1 day ago
        surveyTaskDispatcher.assignScheduledSurveys();
        tasks = participantTaskService.findByStudyEnvironmentId(sandboxBundle.getStudyEnv().getId());
        assertThat(tasks, hasSize(1));
        assertThat(tasks.get(0).getEnrolleeId(), equalTo(sandbox1.enrollee().getId()));
        assertThat(tasks.get(0).getTargetStableId(), equalTo(survey.getStableId()));

        // task will not get assigned twice to the same enrollee
        surveyTaskDispatcher.assignScheduledSurveys();
        tasks = participantTaskService.findByStudyEnvironmentId(sandboxBundle.getStudyEnv().getId());
        assertThat(tasks, hasSize(1));
    }

    @Test
    @Transactional
    public void testRecurringAssign(TestInfo testInfo) {
        // create a 7-day recurring survey, confirm it gets reassigned
        StudyEnvironmentBundle sandboxBundle = studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);
        Survey survey = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(testInfo))
                .portalId(sandboxBundle.getPortal().getId())
                .recurrenceType(RecurrenceType.LONGITUDINAL)
                .recurrenceIntervalDays(7));
        surveyFactory.attachToEnv(survey, sandboxBundle.getStudyEnv().getId(), true);

        EnrolleeBundle sandbox1 = enrolleeFactory.enroll(getTestName(testInfo) + "1", sandboxBundle.getPortal().getShortcode(), sandboxBundle.getStudy().getShortcode(), EnvironmentName.sandbox);
        EnrolleeBundle sandbox2 = enrolleeFactory.enroll(getTestName(testInfo) + "2", sandboxBundle.getPortal().getShortcode(), sandboxBundle.getStudy().getShortcode(), EnvironmentName.sandbox);
        EnrolleeBundle sandbox3 = enrolleeFactory.enroll(getTestName(testInfo) + "3", sandboxBundle.getPortal().getShortcode(), sandboxBundle.getStudy().getShortcode(), EnvironmentName.sandbox);

        List<ParticipantTask> tasks = participantTaskService.findByStudyEnvironmentId(sandboxBundle.getStudyEnv().getId());
        // task should be assigned to all enrollees on creation
        assertThat(tasks, hasSize(3));

        // delete the first enrollee's task
        participantTaskService.delete(tasks.stream().filter(task ->
                task.getEnrolleeId().equals(sandbox1.enrollee().getId())).findFirst().get().getId(), DataAuditInfo.builder().systemProcess(getTestName(testInfo)).build());
        // change the second enrollee's task time to 8 days ago
        timeShiftDao.changeTaskCreationTime(tasks.stream().filter(task ->
                task.getEnrolleeId().equals(sandbox2.enrollee().getId())).findFirst().get().getId(), Instant.now().minus(8, ChronoUnit.DAYS));

        // this should not assign to 1 (since it has no prior task) or 3 (since its task was assigned recently)
        surveyTaskDispatcher.assignScheduledSurveys();
        tasks = participantTaskService.findByStudyEnvironmentId(sandboxBundle.getStudyEnv().getId());
        assertThat(tasks, hasSize(3));
        assertThat(participantTaskService.findByEnrolleeId(sandbox1.enrollee().getId()), hasSize(0));
        assertThat(participantTaskService.findByEnrolleeId(sandbox2.enrollee().getId()), hasSize(2));
        assertThat(participantTaskService.findByEnrolleeId(sandbox3.enrollee().getId()), hasSize(1));
    }
}
