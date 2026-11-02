package bio.terra.pearl.core.service.participant.merge;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentBundle;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.notification.EmailTemplateFactory;
import bio.terra.pearl.core.factory.notification.TriggerFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.factory.survey.SurveyResponseFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.notification.NotificationDeliveryType;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.notification.TriggerEventType;
import bio.terra.pearl.core.model.notification.TriggerType;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeWithdrawalReason;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.WithdrawnEnrollee;
import bio.terra.pearl.core.dao.survey.PreEnrollmentResponseDao;
import bio.terra.pearl.core.dao.survey.PreregistrationResponseDao;
import bio.terra.pearl.core.model.survey.PreEnrollmentResponse;
import bio.terra.pearl.core.model.survey.PreregistrationResponse;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.model.survey.SurveyTaskConfigDto;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.WithdrawnEnrolleeService;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import bio.terra.pearl.core.service.survey.SurveyTaskDispatcher;
import bio.terra.pearl.core.service.workflow.EnrollmentService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import bio.terra.pearl.core.service.workflow.RegistrationService;
import com.google.api.client.util.Objects;
import lombok.With;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ParticipantMergeServiceTests extends BaseSpringBootTest {
    @Autowired
    private ParticipantMergePlanService participantMergePlanService;
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    private SurveyFactory surveyFactory;
    @Autowired
    private ParticipantMergeService participantMergeService;
    @Autowired
    private EnrolleeService enrolleeService;
    @Autowired
    private EnrollmentService enrollmentService;
    @Autowired
    private ParticipantUserService participantUserService;
    @Autowired
    private ParticipantTaskService participantTaskService;
    @Autowired
    private WithdrawnEnrolleeService withdrawnEnrolleeService;
    @Autowired
    private SurveyResponseFactory surveyResponseFactory;
    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private SurveyResponseService surveyResponseService;
    @Autowired
    private TriggerFactory triggerFactory;
    @Autowired
    private EmailTemplateFactory emailTemplateFactory;
    @Autowired
    private SurveyTaskDispatcher surveyTaskDispatcher;
    @Autowired
    private PreregistrationResponseDao preregistrationResponseDao;
    @Autowired
    private PreEnrollmentResponseDao preEnrollmentResponseDao;

    /** merge two enrollees who each have a single not-started survey task */
    @Test
    @Transactional
    public void testSimpleMerge(TestInfo info) {

        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        Survey survey = surveyFactory.buildPersisted(getTestName(info), studyEnvBundle.getPortal().getId());
        surveyFactory.attachToEnv(survey, studyEnvBundle.getStudyEnv().getId(), true);

        // note we use the 'enroll' factory method so that tasks are added
        EnrolleeBundle sourceBundle = enrolleeFactory.enroll("source@test.com", studyEnvBundle.getPortal().getShortcode(),
                studyEnvBundle.getStudy().getShortcode(), studyEnvBundle.getStudyEnv().getEnvironmentName());

        EnrolleeBundle targetBundle = enrolleeFactory.enroll("target@test.com", studyEnvBundle.getPortal().getShortcode(),
                studyEnvBundle.getStudy().getShortcode(), studyEnvBundle.getStudyEnv().getEnvironmentName());

        ParticipantUserMerge merge = participantMergePlanService.planMerge(sourceBundle.participantUser(), targetBundle.participantUser(),
                studyEnvBundle.getPortal());
        assertThat(merge.getEnrollees(), hasSize(1));

        participantMergeService.applyMerge(merge, DataAuditInfo.builder().systemProcess("test").build());

        // confirm only the target enrollee is left
        List<Enrollee> allEnrollees = enrolleeService.findByStudyEnvironment(studyEnvBundle.getStudyEnv().getId());
        assertThat(allEnrollees, hasSize(1));
        assertThat(allEnrollees.get(0).getId(), equalTo(targetBundle.enrollee().getId()));
        assertThat(participantTaskService.findByEnrolleeId(allEnrollees.get(0).getId()), hasSize(1));

        // confirm only the target user is left
        assertThat(participantUserService.find(sourceBundle.participantUser().getId()).isPresent(), is(false));
        assertThat(participantUserService.find(targetBundle.participantUser().getId()).isPresent(), is(true));

        // confirm the source enrollee is withdrawn
        List<WithdrawnEnrollee> withdrawnUsers = withdrawnEnrolleeService.findByStudyEnvironmentIdNoData(studyEnvBundle.getStudyEnv().getId());
        assertThat(withdrawnUsers, hasSize(1));
        assertThat(withdrawnUsers.get(0).getShortcode(), equalTo(sourceBundle.enrollee().getShortcode()));
        assertThat(withdrawnUsers.get(0).getReason(), equalTo(EnrolleeWithdrawalReason.DUPLICATE));
    }

    private Survey addTriggerAndSurvey(String testName, StudyEnvironmentBundle studyEnvBundle) {
        triggerFactory.buildPersisted(Trigger.builder()
                        .deliveryType(NotificationDeliveryType.EMAIL)
                        .triggerType(TriggerType.EVENT)
                        .eventType(TriggerEventType.STUDY_ENROLLMENT)
                        .emailTemplate(emailTemplateFactory.buildPersisted(testName, studyEnvBundle.getPortal().getId())),
                studyEnvBundle.getStudyEnv().getId(), studyEnvBundle.getPortalEnv().getId());
        Survey survey1 = surveyFactory.buildPersisted(testName, studyEnvBundle.getPortal().getId());
        surveyFactory.attachToEnv(survey1, studyEnvBundle.getStudyEnv().getId(), true);
        return survey1;
    }

    /** test merge of two participants who have enrolled in a combination of studies within a portal */
    @Test
    @Transactional
    public void testMultiStudyMerge(TestInfo info) {
        // build 3 studies in the same portal, each with an enrollment email
        StudyEnvironmentBundle studyEnvBundle1 = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        Survey survey1 = addTriggerAndSurvey(getTestName(info), studyEnvBundle1);

        StudyEnvironmentBundle studyEnvBundle2 = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox, studyEnvBundle1.getPortal(), studyEnvBundle1.getPortalEnv());
        Survey survey2 = addTriggerAndSurvey(getTestName(info), studyEnvBundle2);

        StudyEnvironmentBundle studyEnvBundle3 = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox, studyEnvBundle1.getPortal(), studyEnvBundle1.getPortalEnv());
        Survey survey3 = addTriggerAndSurvey(getTestName(info), studyEnvBundle3);

        // a person created two accounts.  They enrolled in study 1 under both, but only one in study 2 and the other one in study 3
        RegistrationService.RegistrationResult sourceUser = registrationService.register(studyEnvBundle1.getPortal().getShortcode(), studyEnvBundle1.getStudyEnv().getEnvironmentName(),
                "username1", null, null);
        RegistrationService.RegistrationResult targetUser = registrationService.register(studyEnvBundle1.getPortal().getShortcode(), studyEnvBundle1.getStudyEnv().getEnvironmentName(),
                "username2", null, null);

        HubResponse sourceEnrollee1 = enrollmentService.enroll(studyEnvBundle1.getStudyEnv().getEnvironmentName(), studyEnvBundle1.getStudy().getShortcode(), sourceUser.participantUser(), sourceUser.portalParticipantUser(), null);
        surveyResponseFactory.submitStringAnswer(survey1.getStableId(), "question1", "value1", true, new EnrolleeBundle(sourceEnrollee1.getEnrollee(), sourceUser.participantUser(), sourceUser.portalParticipantUser(), studyEnvBundle1.getPortal().getId()));
        HubResponse targetEnrollee1 = enrollmentService.enroll(studyEnvBundle1.getStudyEnv().getEnvironmentName(), studyEnvBundle1.getStudy().getShortcode(), targetUser.participantUser(), targetUser.portalParticipantUser(), null);
        surveyResponseFactory.submitStringAnswer(survey1.getStableId(), "question1", "value2", true, new EnrolleeBundle(targetEnrollee1.getEnrollee(), targetUser.participantUser(), targetUser.portalParticipantUser(), studyEnvBundle1.getPortal().getId()));
        HubResponse sourceEnrollee2 = enrollmentService.enroll(studyEnvBundle2.getStudyEnv().getEnvironmentName(), studyEnvBundle2.getStudy().getShortcode(), sourceUser.participantUser(), sourceUser.portalParticipantUser(), null);
        surveyResponseFactory.submitStringAnswer(survey2.getStableId(), "question1", "value3", true, new EnrolleeBundle(sourceEnrollee2.getEnrollee(), sourceUser.participantUser(), sourceUser.portalParticipantUser(), studyEnvBundle1.getPortal().getId()));
        HubResponse targetEnrollee3 = enrollmentService.enroll(studyEnvBundle3.getStudyEnv().getEnvironmentName(), studyEnvBundle3.getStudy().getShortcode(), targetUser.participantUser(), targetUser.portalParticipantUser(), null);
        surveyResponseFactory.submitStringAnswer(survey3.getStableId(), "question1", "value4", true, new EnrolleeBundle(targetEnrollee3.getEnrollee(), targetUser.participantUser(), targetUser.portalParticipantUser(), studyEnvBundle1.getPortal().getId()));


        ParticipantUserMerge merge = participantMergePlanService.planMerge(sourceUser.participantUser(), targetUser.participantUser(),
                studyEnvBundle1.getPortal());
        // we expect three merge pairs, one for each study
        assertThat(merge.getEnrollees(), hasSize(3));
        for(MergeAction<Enrollee, EnrolleeMerge> enrolleeMerge : merge.getEnrollees()) {
            if (enrolleeMerge.getSource() != null && enrolleeMerge.getSource().getId().equals(sourceEnrollee1.getEnrollee().getId())) {
                assertThat(enrolleeMerge.getTarget().getId(), equalTo(targetEnrollee1.getEnrollee().getId()));
                assertThat(enrolleeMerge.getAction(), equalTo(MergeAction.Action.MERGE));
            } else if (enrolleeMerge.getSource() != null && enrolleeMerge.getSource().getId().equals(sourceEnrollee2.getEnrollee().getId())) {
                assertThat(enrolleeMerge.getTarget(), nullValue());
                assertThat(enrolleeMerge.getAction(), equalTo(MergeAction.Action.MOVE_SOURCE));
            } else if (enrolleeMerge.getTarget() != null && enrolleeMerge.getTarget().getId().equals(targetEnrollee3.getEnrollee().getId())) {
                assertThat(enrolleeMerge.getSource(), nullValue());
                assertThat(enrolleeMerge.getAction(), equalTo(MergeAction.Action.NO_ACTION));
            } else {
                throw new RuntimeException("unexpected enrollee merge");
            }
        }

        participantMergeService.applyMerge(merge, DataAuditInfo.builder().systemProcess("test").build());

        // confirm source enrollee 2 remains
        assertThat(enrolleeService.findOneByShortcode(sourceEnrollee2.getEnrollee().getShortcode()).isPresent(), equalTo(true));
        // confirm source enrollee 1 is withdrawn
        List<WithdrawnEnrollee> withdrawnEnrollees = withdrawnEnrolleeService.findByStudyEnvironmentIdNoData(studyEnvBundle1.getStudyEnv().getId());
        assertThat(withdrawnEnrollees, hasSize(1));
        assertThat(withdrawnEnrollees.get(0).getShortcode(), equalTo(sourceEnrollee1.getEnrollee().getShortcode()));
        assertThat(withdrawnEnrollees.get(0).getReason(), equalTo(EnrolleeWithdrawalReason.DUPLICATE));
        // confirm target enrollee 3 remains
        assertThat(enrolleeService.findOneByShortcode(targetEnrollee3.getEnrollee().getShortcode()).isPresent(), equalTo(true));
        // confirm source participant user is gone
        assertThat(participantUserService.find(sourceUser.participantUser().getId()).isPresent(), is(false));

    }


    /** merge two enrollees who each have a survey task with a response */
    @Test
    @Transactional
    public void testSurveyTaskMerge(TestInfo info) {
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        Survey survey1 = surveyFactory.buildPersisted(getTestName(info), studyEnvBundle.getPortal().getId());
        surveyFactory.attachToEnv(survey1, studyEnvBundle.getStudyEnv().getId(), true);
        Survey survey2 = surveyFactory.buildPersisted(getTestName(info), studyEnvBundle.getPortal().getId());
        surveyFactory.attachToEnv(survey2, studyEnvBundle.getStudyEnv().getId(), true);
        Survey survey3 = surveyFactory.buildPersisted(getTestName(info), studyEnvBundle.getPortal().getId());
        surveyFactory.attachToEnv(survey3, studyEnvBundle.getStudyEnv().getId(), true);

        // note we use the 'enroll' factory method so that tasks are added
        EnrolleeBundle sourceBundle = enrolleeFactory.enroll("source@test.com", studyEnvBundle.getPortal().getShortcode(),
                studyEnvBundle.getStudy().getShortcode(), studyEnvBundle.getStudyEnv().getEnvironmentName());

        EnrolleeBundle targetBundle = enrolleeFactory.enroll("target@test.com", studyEnvBundle.getPortal().getShortcode(),
                studyEnvBundle.getStudy().getShortcode(), studyEnvBundle.getStudyEnv().getEnvironmentName());

        // survey1 has a response from both, survey2 has a response from the source, survey 3 has a response from the target
        SurveyResponse responseSt1 = surveyResponseFactory.submitStringAnswer(
                survey1.getStableId(),
                "question1", "source1", true, sourceBundle)
                .getResponse();
        SurveyResponse responseTt1 = surveyResponseFactory.submitStringAnswer(
                survey1.getStableId(),
                "question1", "target1", true, targetBundle)
                .getResponse();
        SurveyResponse responseSt2 = surveyResponseFactory.submitStringAnswer(
                survey2.getStableId(),
                "question1", "source2", true, sourceBundle)
                .getResponse();
        SurveyResponse response_Tt3 = surveyResponseFactory.submitStringAnswer(
               survey3.getStableId(),
                "question1", "target3", true, targetBundle)
                .getResponse();

        ParticipantUserMerge merge = participantMergePlanService.planMerge(sourceBundle.participantUser(), targetBundle.participantUser(),
                studyEnvBundle.getPortal());

        assertThat(merge.getEnrollees(), hasSize(1));
        List<MergeAction<ParticipantTask, ?>> taskMerges = merge.getEnrollees().get(0).getMergePlan().getTasks();
        // there should be 3 task 'pairs'
        assertThat(taskMerges, hasSize(3));
        for (MergeAction<ParticipantTask, ?> taskMerge : taskMerges) {
            if (Objects.equal(taskMerge.getSource().getTargetStableId(), survey1.getStableId())) {
                assertThat(taskMerge.getTarget().getTargetStableId(), equalTo(survey1.getStableId()));
                assertThat(taskMerge.getAction(), equalTo(MergeAction.Action.MOVE_SOURCE));
            } else if (Objects.equal(taskMerge.getSource().getTargetStableId(), survey2.getStableId())) {
                assertThat(taskMerge.getTarget().getTargetStableId(), equalTo(survey2.getStableId()));
                assertThat(taskMerge.getAction(), equalTo(MergeAction.Action.MOVE_SOURCE_DELETE_TARGET));
            } else if (Objects.equal(taskMerge.getTarget().getTargetStableId(), survey3.getStableId())) {
                assertThat(taskMerge.getSource().getTargetStableId(), equalTo(survey3.getStableId()));
                assertThat(taskMerge.getAction(), equalTo(MergeAction.Action.DELETE_SOURCE));
            } else {
                throw new RuntimeException("unexpected task merge");
            }
        }
        participantMergeService.applyMerge(merge, DataAuditInfo.builder().systemProcess("test").build());
        assertThat(participantTaskService.findByEnrolleeId(sourceBundle.enrollee().getId()), hasSize(0));
        List<ParticipantTask> tasks = participantTaskService.findByEnrolleeId(targetBundle.enrollee().getId());
        assertThat(tasks, hasSize(4));
        // two tasks for survey 1, and 1 task for each of the others
        assertThat(tasks.stream().map(ParticipantTask::getTargetStableId).toList(),
                containsInAnyOrder(survey1.getStableId(), survey1.getStableId(), survey2.getStableId(), survey3.getStableId()));
        assertThat(tasks.stream().map(ParticipantTask::getSurveyResponseId).toList(),
                containsInAnyOrder(responseSt2.getId(), responseTt1.getId(), response_Tt3.getId(), responseSt1.getId()));
        assertThat(surveyResponseService.findByEnrolleeId(targetBundle.enrollee().getId()), hasSize(4));
    }

    /** merge two enrollees who have different numbers of responses to a survey */
    @Test
    @Transactional
    public void testRecurringSurveyTaskMerge(TestInfo info) {
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        Survey survey1 = surveyFactory.buildPersisted(getTestName(info), studyEnvBundle.getPortal().getId());
        StudyEnvironmentSurvey studyEnvSurvey = surveyFactory.attachToEnv(survey1, studyEnvBundle.getStudyEnv().getId(), true);

        EnrolleeBundle sourceBundle = enrolleeFactory.enroll("source@test.com", studyEnvBundle.getPortal().getShortcode(),
                studyEnvBundle.getStudy().getShortcode(), studyEnvBundle.getStudyEnv().getEnvironmentName());

        EnrolleeBundle targetBundle = enrolleeFactory.enroll("target@test.com", studyEnvBundle.getPortal().getShortcode(),
                studyEnvBundle.getStudy().getShortcode(), studyEnvBundle.getStudyEnv().getEnvironmentName());

        // the source has one response, the target has multiple
        SurveyResponse responseS1 = surveyResponseFactory.submitStringAnswer(
                        survey1.getStableId(),
                        "question1", "source1", true, sourceBundle)
                .getResponse();
        SurveyResponse responseT1 = surveyResponseFactory.submitStringAnswer(
                        survey1.getStableId(),
                        "question1", "target1", true, targetBundle)
                .getResponse();
        // manually create a second task (this is easier than futzing with dates)
        ParticipantTask task2 = surveyTaskDispatcher.assign(List.of(targetBundle.enrollee()), new SurveyTaskConfigDto(studyEnvSurvey, survey1),
                true, "testing", new ResponsibleEntity(getTestName(info))).get(0);
        SurveyResponse responseT2 = surveyResponseFactory.submitStringAnswer(
                        task2, "question1", "target2", true, targetBundle)
                .getResponse();

        ParticipantUserMerge merge = participantMergePlanService.planMerge(sourceBundle.participantUser(), targetBundle.participantUser(),
                studyEnvBundle.getPortal());

        assertThat(merge.getEnrollees(), hasSize(1));
        List<MergeAction<ParticipantTask, ?>> taskMerges = merge.getEnrollees().get(0).getMergePlan().getTasks();
        // there should be 2 task 'pairs'
        assertThat(taskMerges, hasSize(2));

        participantMergeService.applyMerge(merge, DataAuditInfo.builder().systemProcess("test").build());
        assertThat(participantTaskService.findByEnrolleeId(sourceBundle.enrollee().getId()), hasSize(0));
        List<ParticipantTask> tasks = participantTaskService.findByEnrolleeId(targetBundle.enrollee().getId());
        assertThat(tasks, hasSize(3));
    }

    /** merge two enrollees who each have a pre-registration and a pre-enrollment response */
    @Test
    @Transactional
    public void testPreRegAndPreEnrollMerge(TestInfo info) {
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        Survey survey = surveyFactory.buildPersisted(getTestName(info), studyEnvBundle.getPortal().getId());

        EnrolleeBundle sourceBundle = enrolleeFactory.enroll("source@test.com", studyEnvBundle.getPortal().getShortcode(),
                studyEnvBundle.getStudy().getShortcode(), studyEnvBundle.getStudyEnv().getEnvironmentName());
        EnrolleeBundle targetBundle = enrolleeFactory.enroll("target@test.com", studyEnvBundle.getPortal().getShortcode(),
                studyEnvBundle.getStudy().getShortcode(), studyEnvBundle.getStudyEnv().getEnvironmentName());

        PreregistrationResponse sourcePreReg = createPreReg(survey, studyEnvBundle, sourceBundle);
        PreEnrollmentResponse sourcePreEnroll = createPreEnroll(survey, studyEnvBundle, sourceBundle);
        PreregistrationResponse targetPreReg = createPreReg(survey, studyEnvBundle, targetBundle);
        PreEnrollmentResponse targetPreEnroll = createPreEnroll(survey, studyEnvBundle, targetBundle);

        ParticipantUserMerge merge = participantMergePlanService.planMerge(sourceBundle.participantUser(), targetBundle.participantUser(),
                studyEnvBundle.getPortal());
        assertThat(merge.getEnrollees(), hasSize(1));

        participantMergeService.applyMerge(merge, DataAuditInfo.builder().systemProcess("test").build());

        // the source's responses are cleaned up, the target's are untouched
        assertThat(preregistrationResponseDao.find(sourcePreReg.getId()).isPresent(), is(false));
        assertThat(preEnrollmentResponseDao.find(sourcePreEnroll.getId()).isPresent(), is(false));
        assertThat(preregistrationResponseDao.find(targetPreReg.getId()).isPresent(), is(true));
        assertThat(preEnrollmentResponseDao.find(targetPreEnroll.getId()).isPresent(), is(true));

        // confirm the merge itself still worked
        assertThat(enrolleeService.find(sourceBundle.enrollee().getId()).isPresent(), is(false));
        assertThat(enrolleeService.find(targetBundle.enrollee().getId()).isPresent(), is(true));
        assertThat(participantUserService.find(sourceBundle.participantUser().getId()).isPresent(), is(false));
    }

    /**
     * merge where the source's enrollee is moved (not deleted) and the source also has a pre-enrollment response
     * not tied to any enrollee.  Those responses still reference the source portal participant user, so they
     * must be cleaned up (or reassigned) before that user can be deleted.
     */
    @Test
    @Transactional
    public void testPreEnrollResponsesOfMovedEnrolleeAndOrphan(TestInfo info) {
        StudyEnvironmentBundle studyEnvBundle1 = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        StudyEnvironmentBundle studyEnvBundle2 = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox,
                studyEnvBundle1.getPortal(), studyEnvBundle1.getPortalEnv());
        Survey survey = surveyFactory.buildPersisted(getTestName(info), studyEnvBundle1.getPortal().getId());

        // source is only in study 2, target is only in study 1, so the source enrollee is moved rather than merged
        EnrolleeBundle sourceBundle = enrolleeFactory.enroll("source@test.com", studyEnvBundle1.getPortal().getShortcode(),
                studyEnvBundle2.getStudy().getShortcode(), studyEnvBundle2.getStudyEnv().getEnvironmentName());
        EnrolleeBundle targetBundle = enrolleeFactory.enroll("target@test.com", studyEnvBundle1.getPortal().getShortcode(),
                studyEnvBundle1.getStudy().getShortcode(), studyEnvBundle1.getStudyEnv().getEnvironmentName());

        createPreReg(survey, studyEnvBundle1, sourceBundle);
        PreEnrollmentResponse linkedPreEnroll = createPreEnroll(survey, studyEnvBundle2, sourceBundle);
        // a pre-enroll response that the source completed but never used to enroll
        PreEnrollmentResponse orphanPreEnroll = preEnrollmentResponseDao.create(PreEnrollmentResponse.builder()
                .surveyId(survey.getId())
                .studyEnvironmentId(studyEnvBundle1.getStudyEnv().getId())
                .portalParticipantUserId(sourceBundle.portalParticipantUser().getId())
                .creatingParticipantUserId(sourceBundle.participantUser().getId())
                .fullData("[]")
                .qualified(true)
                .build());

        ParticipantUserMerge merge = participantMergePlanService.planMerge(sourceBundle.participantUser(), targetBundle.participantUser(),
                studyEnvBundle1.getPortal());
        assertThat(merge.getEnrollees().stream().map(MergeAction::getAction).toList(),
                containsInAnyOrder(MergeAction.Action.MOVE_SOURCE, MergeAction.Action.NO_ACTION));

        participantMergeService.applyMerge(merge, DataAuditInfo.builder().systemProcess("test").build());

        assertThat(participantUserService.find(sourceBundle.participantUser().getId()).isPresent(), is(false));
        assertThat(preEnrollmentResponseDao.find(orphanPreEnroll.getId()).isPresent(), is(false));
        // the moved enrollee keeps its pre-enroll response, now owned by the target ppUser
        Enrollee moved = enrolleeService.find(sourceBundle.enrollee().getId()).orElseThrow();
        assertThat(moved.getParticipantUserId(), equalTo(targetBundle.participantUser().getId()));
        assertThat(moved.getPreEnrollmentResponseId(), equalTo(linkedPreEnroll.getId()));
        PreEnrollmentResponse keptResponse = preEnrollmentResponseDao.find(linkedPreEnroll.getId()).orElseThrow();
        assertThat(keptResponse.getPortalParticipantUserId(), equalTo(targetBundle.portalParticipantUser().getId()));
    }

    private PreregistrationResponse createPreReg(Survey survey, StudyEnvironmentBundle studyEnvBundle, EnrolleeBundle bundle) {
        return preregistrationResponseDao.create(PreregistrationResponse.builder()
                .surveyId(survey.getId())
                .portalEnvironmentId(studyEnvBundle.getPortalEnv().getId())
                .portalParticipantUserId(bundle.portalParticipantUser().getId())
                .creatingParticipantUserId(bundle.participantUser().getId())
                .fullData("[]")
                .qualified(true)
                .build());
    }

    private PreEnrollmentResponse createPreEnroll(Survey survey, StudyEnvironmentBundle studyEnvBundle, EnrolleeBundle bundle) {
        PreEnrollmentResponse response = preEnrollmentResponseDao.create(PreEnrollmentResponse.builder()
                .surveyId(survey.getId())
                .studyEnvironmentId(studyEnvBundle.getStudyEnv().getId())
                .portalParticipantUserId(bundle.portalParticipantUser().getId())
                .creatingParticipantUserId(bundle.participantUser().getId())
                .fullData("[]")
                .qualified(true)
                .build());
        Enrollee enrollee = enrolleeService.find(bundle.enrollee().getId()).orElseThrow();
        enrollee.setPreEnrollmentResponseId(response.getId());
        enrolleeService.update(enrollee);
        return response;
    }
}
