package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.dataimport.TimeShiftDao;
import bio.terra.pearl.core.dao.survey.SurveyResponseDao;
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
import bio.terra.pearl.core.model.survey.*;
import bio.terra.pearl.core.model.workflow.*;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskAssignDto;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

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
    @Autowired
    private SurveyResponseDao surveyResponseDao;
    @Autowired
    private StudyEnvironmentSurveyService studyEnvironmentSurveyService;


    @Test
    void testIsDuplicateTask() {
        UUID surveyId = UUID.randomUUID();
        Survey survey = Survey
                .builder()
                .id(surveyId)
                .recurrenceType(RecurrenceType.NONE)
                .build();
        StudyEnvironmentSurvey studyEnvironmentSurvey = StudyEnvironmentSurvey.builder()
                .survey(survey)
                .surveyId(surveyId)
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
        boolean isDuplicate = surveyTaskDispatcher.isDuplicateTask(new SurveyTaskConfigDto(studyEnvironmentSurvey), surveyTask1,
                existingTasks);
        assertTrue(isDuplicate);

        ParticipantTask surveyTask3 = ParticipantTask.builder()
                .targetStableId("TASK_3")
                .taskType(TaskType.SURVEY)
                .build();
        isDuplicate = surveyTaskDispatcher.isDuplicateTask(new SurveyTaskConfigDto(studyEnvironmentSurvey), surveyTask3,
                existingTasks);
        assertFalse(isDuplicate);
    }

    @Test
    void testIsDuplicateForTaskTypes() {
        UUID surveyId = UUID.randomUUID();
        Survey survey = Survey
                .builder()
                .id(surveyId)
                .recurrenceType(RecurrenceType.NONE)
                .build();
        StudyEnvironmentSurvey studyEnvironmentSurvey = StudyEnvironmentSurvey.builder()
                .survey(survey)
                .surveyId(surveyId)
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
            boolean isDuplicate = surveyTaskDispatcher.isDuplicateTask(new SurveyTaskConfigDto(studyEnvironmentSurvey), surveyTask2,
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
        surveyResponseFactory.submitStringAnswer(participantTasks.get(0), "diagnosis", "sick", true, sandbox1);
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
        ParticipantTaskAssignDto assignDto = new ParticipantTaskAssignDto(TaskType.SURVEY, survey.getStableId(), survey.getVersion(), null, true, true, "reason" );
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
        ParticipantTaskAssignDto assignDto = new ParticipantTaskAssignDto(TaskType.SURVEY, survey.getStableId(), survey.getVersion(), null, true, false, "reason" );
        surveyTaskDispatcher.assign(assignDto, sandboxBundle.getStudyEnv().getId(), new ResponsibleEntity(operator));
        List<ParticipantTask> participantTasks = participantTaskService.findTasksByStudyAndTarget(sandboxBundle.getStudyEnv().getId(), List.of(survey.getStableId()));
        assertThat(participantTasks, hasSize(1));

        // shouldn't create a duplicate task even if the enrolleeId is provided manually, since eligibility override is false
        assignDto = new ParticipantTaskAssignDto(TaskType.SURVEY, survey.getStableId(), survey.getVersion(), List.of(sandbox1.enrollee().getId()), false, false, "reason" );
        surveyTaskDispatcher.assign(assignDto, sandboxBundle.getStudyEnv().getId(), new ResponsibleEntity(operator));
        participantTasks = participantTaskService.findTasksByStudyAndTarget(sandboxBundle.getStudyEnv().getId(), List.of(survey.getStableId()));
        assertThat(participantTasks, hasSize(1));

        // if overriding eligibility is specified, then a duplicate task should be created
        assignDto = new ParticipantTaskAssignDto(TaskType.SURVEY, survey.getStableId(), survey.getVersion(), List.of(sandbox1.enrollee().getId()), false, true, "reason" );
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
                new ParticipantTaskAssignDto(TaskType.SURVEY, survey.getStableId(), survey.getVersion(), null, true, false, "reason"),
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
                new ParticipantTaskAssignDto(TaskType.SURVEY, survey.getStableId(), survey.getVersion(), null, true, false, "reason"),
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
                bundle);
        // survey not complete, so no new task should be assigned
        assertThat(participantTaskService.findByEnrolleeId(bundle.enrollee().getId()), hasSize(1));

        surveyResponseFactory.submitStringAnswer(
                medFormTask,
                "diagnosis",
                "fine",
                true,
                bundle);
        // survey answer not a match -- no new task should be assigned
        assertThat(participantTaskService.findByEnrolleeId(bundle.enrollee().getId()), hasSize(1));

        surveyResponseFactory.submitStringAnswer(
                medFormTask,
                "diagnosis",
                "sick",
                true,
                bundle);
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
        surveyTaskDispatcher.assignScheduledTasks();
        tasks = participantTaskService.findByStudyEnvironmentId(sandboxBundle.getStudyEnv().getId());
        assertThat(tasks, hasSize(1));
        assertThat(tasks.get(0).getEnrolleeId(), equalTo(sandbox1.enrollee().getId()));
        assertThat(tasks.get(0).getTargetStableId(), equalTo(survey.getStableId()));

        // task will not get assigned twice to the same enrollee
        surveyTaskDispatcher.assignScheduledTasks();
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

        surveyTaskDispatcher.assignScheduledTasks(); // should be no-op

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
        ParticipantTask sandbox2Task = tasks.stream().filter(task ->
                task.getEnrolleeId().equals(sandbox2.enrollee().getId())).findFirst().get();
        sandbox2Task.setCompletedAt(Instant.now().minus(8, ChronoUnit.DAYS));
        sandbox2Task.setStatus(TaskStatus.COMPLETE);
        participantTaskService.update(sandbox2Task, DataAuditInfo.builder().systemProcess(getTestName(testInfo)).build());


        // this should not assign to 1 (since it has no prior task) or 3 (since its task was assigned recently)
        surveyTaskDispatcher.assignScheduledTasks();
        tasks = participantTaskService.findByStudyEnvironmentId(sandboxBundle.getStudyEnv().getId());
        assertThat(tasks, hasSize(3));
        assertThat(participantTaskService.findByEnrolleeId(sandbox1.enrollee().getId()), hasSize(0));
        assertThat(participantTaskService.findByEnrolleeId(sandbox2.enrollee().getId()), hasSize(2));
        assertThat(participantTaskService.findByEnrolleeId(sandbox3.enrollee().getId()), hasSize(1));
    }

    @Test
    @Transactional
    public void testRecurringAssignDoesNotRecurIfTaskInProgressAndRecurOnCompletion(TestInfo testInfo) {
        // create a 7-day recurring survey, confirm it gets reassigned
        StudyEnvironmentBundle sandboxBundle = studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);
        Survey survey = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(testInfo))
                .portalId(sandboxBundle.getPortal().getId())
                .recurrenceType(RecurrenceType.LONGITUDINAL)
                .recurOn(RecurOn.COMPLETION)
                .recurrenceIntervalDays(7));
        surveyFactory.attachToEnv(survey, sandboxBundle.getStudyEnv().getId(), true);

        EnrolleeBundle sandbox1 = enrolleeFactory.enroll(getTestName(testInfo) + "1", sandboxBundle.getPortal().getShortcode(), sandboxBundle.getStudy().getShortcode(), EnvironmentName.sandbox);
        EnrolleeBundle sandbox2 = enrolleeFactory.enroll(getTestName(testInfo) + "2", sandboxBundle.getPortal().getShortcode(), sandboxBundle.getStudy().getShortcode(), EnvironmentName.sandbox);
        EnrolleeBundle sandbox3 = enrolleeFactory.enroll(getTestName(testInfo) + "3", sandboxBundle.getPortal().getShortcode(), sandboxBundle.getStudy().getShortcode(), EnvironmentName.sandbox);

        List<ParticipantTask> tasks = participantTaskService.findByStudyEnvironmentId(sandboxBundle.getStudyEnv().getId());
        // task should be assigned to all enrollees on creation
        assertThat(tasks, hasSize(3));


        ParticipantTask sandbox1Task = tasks.stream().filter(task ->
                task.getEnrolleeId().equals(sandbox1.enrollee().getId())).findFirst().get();

        // make first task old but not completed
        sandbox1Task.setCreatedAt(Instant.now().minus(8, ChronoUnit.DAYS));
        sandbox1Task.setStatus(TaskStatus.IN_PROGRESS);

        participantTaskService.update(sandbox1Task, DataAuditInfo.builder().systemProcess(getTestName(testInfo)).build());

        // make second task old and completed
        ParticipantTask sandbox2Task = tasks.stream().filter(task ->
                task.getEnrolleeId().equals(sandbox2.enrollee().getId())).findFirst().get();

        sandbox2Task.setCreatedAt(Instant.now().minus(8, ChronoUnit.DAYS));
        sandbox2Task.setCompletedAt(Instant.now().minus(8, ChronoUnit.DAYS));
        sandbox2Task.setStatus(TaskStatus.COMPLETE);

        participantTaskService.update(sandbox2Task, DataAuditInfo.builder().systemProcess(getTestName(testInfo)).build());

        // make third task recently completed

        ParticipantTask sandbox3Task = tasks.stream().filter(task ->
                task.getEnrolleeId().equals(sandbox3.enrollee().getId())).findFirst().get();

        sandbox3Task.setCreatedAt(Instant.now().minus(1, ChronoUnit.DAYS));
        sandbox3Task.setCompletedAt(Instant.now().minus(1, ChronoUnit.DAYS));
        sandbox3Task.setStatus(TaskStatus.COMPLETE);

        participantTaskService.update(sandbox3Task, DataAuditInfo.builder().systemProcess(getTestName(testInfo)).build());


        // this should only assign to 2

        surveyTaskDispatcher.assignScheduledTasks();

        tasks = participantTaskService.findByStudyEnvironmentId(sandboxBundle.getStudyEnv().getId());
        assertThat(tasks, hasSize(4));
        assertThat(participantTaskService.findByEnrolleeId(sandbox1.enrollee().getId()), hasSize(1));
        assertThat(participantTaskService.findByEnrolleeId(sandbox2.enrollee().getId()), hasSize(2));
        assertThat(participantTaskService.findByEnrolleeId(sandbox3.enrollee().getId()), hasSize(1));
    }

    @Test
    @Transactional
    public void testRecurOnCreation(TestInfo testInfo) {
        // create a 7-day recurring survey, confirm it gets reassigned
        StudyEnvironmentBundle sandboxBundle = studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);
        Survey survey = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(testInfo))
                .portalId(sandboxBundle.getPortal().getId())
                .recurrenceType(RecurrenceType.LONGITUDINAL)
                .recurOn(RecurOn.CREATION)
                .recurrenceIntervalDays(7));
        surveyFactory.attachToEnv(survey, sandboxBundle.getStudyEnv().getId(), true);

        EnrolleeBundle sandbox1 = enrolleeFactory.enroll(getTestName(testInfo) + "1", sandboxBundle.getPortal().getShortcode(), sandboxBundle.getStudy().getShortcode(), EnvironmentName.sandbox);
        EnrolleeBundle sandbox2 = enrolleeFactory.enroll(getTestName(testInfo) + "2", sandboxBundle.getPortal().getShortcode(), sandboxBundle.getStudy().getShortcode(), EnvironmentName.sandbox);
        // this enrollee has a task that should not recur
        EnrolleeBundle sandbox3 = enrolleeFactory.enroll(getTestName(testInfo) + "3", sandboxBundle.getPortal().getShortcode(), sandboxBundle.getStudy().getShortcode(), EnvironmentName.sandbox);

        List<ParticipantTask> tasks = participantTaskService.findByStudyEnvironmentId(sandboxBundle.getStudyEnv().getId());
        // task should be assigned to all enrollees on creation
        assertThat(tasks, hasSize(3));

        ParticipantTask sandbox1Task = tasks.stream().filter(task ->
                task.getEnrolleeId().equals(sandbox1.enrollee().getId())).findFirst().get();

        // make first task old but not completed
        sandbox1Task.setCreatedAt(Instant.now().minus(8, ChronoUnit.DAYS));
        sandbox1Task.setStatus(TaskStatus.IN_PROGRESS);

        participantTaskService.update(sandbox1Task, DataAuditInfo.builder().systemProcess(getTestName(testInfo)).build());
        timeShiftDao.changeTasksCreationTime(List.of(sandbox1Task.getId()), Instant.now().minus(8, ChronoUnit.DAYS));

        // make second task old but completed recently
        ParticipantTask sandbox2Task = tasks.stream().filter(task ->
                task.getEnrolleeId().equals(sandbox2.enrollee().getId())).findFirst().get();
        sandbox2Task.setCompletedAt(Instant.now().minus(1, ChronoUnit.DAYS));
        sandbox2Task.setStatus(TaskStatus.COMPLETE);

        participantTaskService.update(sandbox2Task, DataAuditInfo.builder().systemProcess(getTestName(testInfo)).build());
        timeShiftDao.changeTasksCreationTime(List.of(sandbox2Task.getId()), Instant.now().minus(8, ChronoUnit.DAYS));

        surveyTaskDispatcher.assignScheduledTasks();

        tasks = participantTaskService.findByStudyEnvironmentId(sandboxBundle.getStudyEnv().getId());
        assertThat(tasks, hasSize(5));

        // should have assigned a new task to the first and second enrollee

        assertThat(participantTaskService.findByEnrolleeId(sandbox1.enrollee().getId()), hasSize(2));
        assertThat(participantTaskService.findByEnrolleeId(sandbox2.enrollee().getId()), hasSize(2));
        assertThat(participantTaskService.findByEnrolleeId(sandbox3.enrollee().getId()), hasSize(1));


    }

    @Test
    @Transactional
    public void testRecurringOnlyCaresAboutLatest(TestInfo testInfo) {
        // create a 7-day recurring survey, confirm it gets reassigned
        StudyEnvironmentBundle sandboxBundle = studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);
        Survey survey = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(testInfo))
                .portalId(sandboxBundle.getPortal().getId())
                .recurrenceType(RecurrenceType.LONGITUDINAL)
                .recurrenceIntervalDays(7));
        surveyFactory.attachToEnv(survey, sandboxBundle.getStudyEnv().getId(), true);

        EnrolleeBundle sandbox = enrolleeFactory.enroll(getTestName(testInfo) + "1", sandboxBundle.getPortal().getShortcode(), sandboxBundle.getStudy().getShortcode(), EnvironmentName.sandbox);

        List<ParticipantTask> tasks = participantTaskService.findByStudyEnvironmentId(sandboxBundle.getStudyEnv().getId());
        // task should be assigned to all enrollees on creation
        assertThat(tasks, hasSize(1));


        // assign some new versions of the task
        surveyTaskDispatcher.assign(
                new ParticipantTaskAssignDto(TaskType.SURVEY, survey.getStableId(), survey.getVersion(), List.of(sandbox.enrollee().getId()), true, true, "reason"),
                sandboxBundle.getStudyEnv().getId(), new ResponsibleEntity(adminUserFactory.buildPersisted(getTestName(testInfo), true)));
        surveyTaskDispatcher.assign(
                new ParticipantTaskAssignDto(TaskType.SURVEY, survey.getStableId(), survey.getVersion(), List.of(sandbox.enrollee().getId()), true, true, "reason"),
                sandboxBundle.getStudyEnv().getId(), new ResponsibleEntity(adminUserFactory.buildPersisted(getTestName(testInfo), true)));

        tasks = participantTaskService
                .findByStudyEnvironmentId(sandboxBundle.getStudyEnv().getId())
                .stream().sorted(Comparator.comparing(ParticipantTask::getCreatedAt).reversed())
                .toList();

        assertThat(tasks, hasSize(3));

        ParticipantTask task1 = tasks.get(0);
        ParticipantTask task2 = tasks.get(1);
        ParticipantTask task3 = tasks.get(2);

        // make task1 old and in-progress
        task1.setCreatedAt(Instant.now().minus(10, ChronoUnit.DAYS));
        task1.setStatus(TaskStatus.IN_PROGRESS);
        participantTaskService.update(task1, DataAuditInfo.builder().systemProcess(getTestName(testInfo)).build());

        // make task2 old and complete
        task2.setCreatedAt(Instant.now().minus(9, ChronoUnit.DAYS));
        task2.setCompletedAt(Instant.now().minus(9, ChronoUnit.DAYS));
        task2.setStatus(TaskStatus.COMPLETE);

        participantTaskService.update(task2, DataAuditInfo.builder().systemProcess(getTestName(testInfo)).build());

        // make task3 old and in-progress
        task3.setCreatedAt(Instant.now().minus(8, ChronoUnit.DAYS));
        task3.setStatus(TaskStatus.IN_PROGRESS);
        participantTaskService.update(task3, DataAuditInfo.builder().systemProcess(getTestName(testInfo)).build());

        // this should not assign anything even though participant has an old completed one

        surveyTaskDispatcher.assignScheduledTasks();

        tasks = participantTaskService.findByStudyEnvironmentId(sandboxBundle.getStudyEnv().getId());

        assertThat(tasks, hasSize(3));

        // make task1 (latest task) old & complete

        task1.setCompletedAt(Instant.now().minus(8, ChronoUnit.DAYS));
        task1.setStatus(TaskStatus.COMPLETE);

        participantTaskService.update(task1, DataAuditInfo.builder().systemProcess(getTestName(testInfo)).build());

        // this should assign a new task to the enrollee, since the latest task is now complete
        surveyTaskDispatcher.assignScheduledTasks();

        tasks = participantTaskService.findByStudyEnvironmentId(sandboxBundle.getStudyEnv().getId());

        assertThat(tasks, hasSize(4));
    }


    @Test
    @Transactional
    public void testRecurringAssignIgnoresRemovedTasks(TestInfo testInfo) {
        // create a 7-day recurring survey, confirm it gets reassigned
        StudyEnvironmentBundle sandboxBundle = studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);
        Survey survey = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(testInfo))
                .portalId(sandboxBundle.getPortal().getId())
                .recurrenceType(RecurrenceType.LONGITUDINAL)
                .recurrenceIntervalDays(7));
        surveyFactory.attachToEnv(survey, sandboxBundle.getStudyEnv().getId(), true);

        EnrolleeBundle sandbox = enrolleeFactory.enroll(getTestName(testInfo) + "1", sandboxBundle.getPortal().getShortcode(), sandboxBundle.getStudy().getShortcode(), EnvironmentName.sandbox);

        List<ParticipantTask> tasks = participantTaskService.findByStudyEnvironmentId(sandboxBundle.getStudyEnv().getId());
        // task should be assigned to all enrollees on creation
        assertThat(tasks, hasSize(1));


        // assign some new versions of the task
        surveyTaskDispatcher.assign(
                new ParticipantTaskAssignDto(TaskType.SURVEY, survey.getStableId(), survey.getVersion(), List.of(sandbox.enrollee().getId()), true, true, "reason"),
                sandboxBundle.getStudyEnv().getId(), new ResponsibleEntity(adminUserFactory.buildPersisted(getTestName(testInfo), true)));


        tasks = participantTaskService.findByStudyEnvironmentId(sandboxBundle.getStudyEnv().getId());

        assertThat(tasks, hasSize(2));

        ParticipantTask task1 = tasks.get(0);
        ParticipantTask task2 = tasks.get(1);

        // make task1 old and in-progress

        task1.setCreatedAt(Instant.now().minus(10, ChronoUnit.DAYS));
        task1.setStatus(TaskStatus.IN_PROGRESS);

        participantTaskService.update(task1, DataAuditInfo.builder().systemProcess(getTestName(testInfo)).build());

        // make task2 old and removed

        task2.setCreatedAt(Instant.now().minus(9, ChronoUnit.DAYS));
        task2.setStatus(TaskStatus.REMOVED);

        participantTaskService.update(task2, DataAuditInfo.builder().systemProcess(getTestName(testInfo)).build());

        // this should not assign since latest non-removed is in progress
        surveyTaskDispatcher.assignScheduledTasks();

        tasks = participantTaskService.findByStudyEnvironmentId(sandboxBundle.getStudyEnv().getId());
        assertThat(tasks, hasSize(2));


        // make task1 old complete

        task1.setCompletedAt(Instant.now().minus(10, ChronoUnit.DAYS));
        task1.setStatus(TaskStatus.COMPLETE);

        participantTaskService.update(task1, DataAuditInfo.builder().systemProcess(getTestName(testInfo)).build());

        // this should assign a new task to the enrollee, since the latest non-removed task is now complete
        surveyTaskDispatcher.assignScheduledTasks();

        tasks = participantTaskService.findByStudyEnvironmentId(sandboxBundle.getStudyEnv().getId());

        assertThat(tasks, hasSize(3));
    }

    @Test
    @Transactional
    public void testMultipleVersionMultipleSurveyRecurringAssign(TestInfo testInfo) {
        // make sure re-assign is smart enough to not get confused if multiple recurring surveys
        StudyEnvironmentBundle sandboxBundle = studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);
        Survey survey1V1 = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(testInfo))
                .portalId(sandboxBundle.getPortal().getId())
                .recurrenceType(RecurrenceType.LONGITUDINAL)
                .version(1)
                .autoAssign(true)
                .stableId("survey1")
                .recurrenceIntervalDays(7));

        Survey survey2 = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(testInfo))
                .portalId(sandboxBundle.getPortal().getId())
                .recurrenceType(RecurrenceType.LONGITUDINAL)
                .version(1)
                .autoAssign(true)
                .stableId("survey2")
                .recurrenceIntervalDays(7));


        StudyEnvironmentSurvey survey1V1ses = surveyFactory.attachToEnv(survey1V1, sandboxBundle.getStudyEnv().getId(), true);
        surveyFactory.attachToEnv(survey2, sandboxBundle.getStudyEnv().getId(), true);

        EnrolleeBundle enrollee1 = enrolleeFactory.enroll(getTestName(testInfo) + "1", sandboxBundle.getPortal().getShortcode(), sandboxBundle.getStudy().getShortcode(), EnvironmentName.sandbox);
        EnrolleeBundle enrollee2 = enrolleeFactory.enroll(getTestName(testInfo) + "2", sandboxBundle.getPortal().getShortcode(), sandboxBundle.getStudy().getShortcode(), EnvironmentName.sandbox);

        Survey survey1V2 = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(testInfo))
                .portalId(sandboxBundle.getPortal().getId())
                .recurrenceType(RecurrenceType.LONGITUDINAL)
                .version(2)
                .autoAssign(true)
                .stableId("survey1")
                .recurrenceIntervalDays(7));

        // make v1 outdated as v2 is the latest
        survey1V1ses.setActive(false);
        studyEnvironmentSurveyService.update(survey1V1ses); // deactivate old version

        // activate new version
        surveyFactory.attachToEnv(survey1V2, sandboxBundle.getStudyEnv().getId(), true);

        // assign new version of survey1
        surveyTaskDispatcher.assign(
                new ParticipantTaskAssignDto(TaskType.SURVEY, survey1V2.getStableId(), survey1V2.getVersion(), List.of(enrollee1.enrollee().getId()), true, true, "reason"),
                sandboxBundle.getStudyEnv().getId(), new ResponsibleEntity(adminUserFactory.buildPersisted(getTestName(testInfo), true)));

        List<ParticipantTask> tasks = participantTaskService.findByStudyEnvironmentId(sandboxBundle.getStudyEnv().getId());
        for (ParticipantTask task : tasks) {
            task.setStatus(TaskStatus.COMPLETE);
            task.setCompletedAt(Instant.now().minus(8, ChronoUnit.DAYS));
            participantTaskService.update(task, DataAuditInfo.builder().systemProcess(getTestName(testInfo)).build());
            // make them all eligible for recurrence
        }

        // at this point:
        // enrollee 1 has 2 tasks, one for survey1 V1, one for survey1 V2, one for survey2
        // enrollee 2 has 2 tasks, one for survey1 V1 and survey2
        // on recurrence: enrollee1 should only recur on V2, enrollee2 should recur on V2 and survey2

        surveyTaskDispatcher.assignScheduledTasks();

        tasks = participantTaskService.findByStudyEnvironmentId(sandboxBundle.getStudyEnv().getId());

        List<ParticipantTask> enrollee1Tasks = tasks.stream().filter(task -> task.getEnrolleeId().equals(enrollee1.enrollee().getId())).toList();
        List<ParticipantTask> enrollee2Tasks = tasks.stream().filter(task -> task.getEnrolleeId().equals(enrollee2.enrollee().getId())).toList();

        assertThat(enrollee1Tasks, hasSize(5));
        assertThat(enrollee2Tasks, hasSize(4));

        // enrollee 1 should have 3 tasks, one for survey1 V1, two for survey1 V2

        assertThat(enrollee1Tasks.stream().filter(task -> task.getTargetStableId().equals(survey1V1.getStableId()) && task.getTargetAssignedVersion() == 1).toList(), hasSize(1));
        assertThat(enrollee1Tasks.stream().filter(task -> task.getTargetStableId().equals(survey1V2.getStableId()) && task.getTargetAssignedVersion() == 2).toList(), hasSize(2));
        assertThat(enrollee1Tasks.stream().filter(task -> task.getTargetStableId().equals(survey2.getStableId())).toList(), hasSize(2));

        // enrollee 2 should have 4 tasks, one for survey1 V1, one for survey1 V2, two for survey2
        assertThat(enrollee2Tasks.stream().filter(task -> task.getTargetStableId().equals(survey1V2.getStableId()) && task.getTargetAssignedVersion() == 1).toList(), hasSize(1));
        assertThat(enrollee2Tasks.stream().filter(task -> task.getTargetStableId().equals(survey1V2.getStableId()) && task.getTargetAssignedVersion() == 2).toList(), hasSize(1));
        assertThat(enrollee2Tasks.stream().filter(task -> task.getTargetStableId().equals(survey2.getStableId())).toList(), hasSize(2));


    }

    @Test
    @Transactional
    public void testLongitudinalPrepopulate(TestInfo testInfo) {
        StudyEnvironmentBundle sandboxBundle = studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);
        AdminUser operator = adminUserFactory.buildPersisted(getTestName(testInfo), true);
        EnrolleeBundle enrollee = enrolleeFactory.buildWithPortalUser(getTestName(testInfo), sandboxBundle.getPortalEnv(), sandboxBundle.getStudyEnv());
        Survey survey = surveyFactory.buildPersisted(Survey.builder()
                .portalId(sandboxBundle.getPortal().getId())
                .stableId("lifestyle")
                .content("{\"pages\":[{\"elements\":[{\"type\":\"text\",\"name\":\"diagnosis\",\"title\":\"What is your diagnosis?\"}]}]}")
                .surveyType(SurveyType.RESEARCH)
                .name("Lifestyle Survey")
                .recurrenceType(RecurrenceType.LONGITUDINAL)
                .recurrenceIntervalDays(7)
                .prepopulate(true));
        surveyFactory.attachToEnv(survey, sandboxBundle.getStudyEnv().getId(), true);
        ParticipantTaskAssignDto assignDto = new ParticipantTaskAssignDto(TaskType.SURVEY, survey.getStableId(), survey.getVersion(), null, true, true, "reason" );
        surveyTaskDispatcher.assign(assignDto, sandboxBundle.getStudyEnv().getId(), new ResponsibleEntity(operator));
        List<ParticipantTask> initialParticipantTasks = participantTaskService.findByEnrolleeId(enrollee.enrollee().getId());

        // confirm the task was assigned
        assertThat(initialParticipantTasks, hasSize(1));
        ParticipantTask initialTask = initialParticipantTasks.getFirst();

        // submit an answer to the survey
        surveyResponseFactory.submitStringAnswer(
                initialTask,
                "diagnosis",
                "sick",
                false,
                enrollee);

        // change the task time to 8 days ago, so it can be re-assigned during the next scheduled task assignment
        initialTask = participantTaskService.find(initialTask.getId()).get();
        initialTask.setStatus(TaskStatus.COMPLETE);
        initialTask.setCompletedAt(Instant.now().minus(8, ChronoUnit.DAYS));
        participantTaskService.update(initialTask, DataAuditInfo.builder().systemProcess(getTestName(testInfo)).build());

        // test that task is re-assigned and answer was prepopulated in the new response
        surveyTaskDispatcher.assignScheduledTasks();

        // confirm the task was re-assigned
        List<ParticipantTask> latestParticipantTasks = participantTaskService
                .findByEnrolleeId(enrollee.enrollee().getId());
        assertThat(latestParticipantTasks, hasSize(2));

        ParticipantTask finalInitialTask = initialTask;
        ParticipantTask reassignedTask = latestParticipantTasks.stream().filter(task -> !task.getId().equals(finalInitialTask.getId())).findFirst().get();

        SurveyResponse reassignedResponse = surveyResponseDao.findOneWithAnswers(reassignedTask.getSurveyResponseId()).get();

        //confirm diagnosis answer was prepopulated in the new response
        assertThat(reassignedResponse.getAnswers().getFirst().getStringValue(), equalTo("sick"));

        assertThat(reassignedTask.getCreatedAt(), not(equalTo(initialTask.getCreatedAt())));
        assertThat(reassignedTask.getLastUpdatedAt(), not(equalTo(initialTask.getLastUpdatedAt())));
        assertThat(reassignedTask.getStatus(), equalTo(TaskStatus.NEW));
    }
}
