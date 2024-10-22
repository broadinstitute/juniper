package bio.terra.pearl.core.service.participant.merge;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentBundle;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.factory.survey.SurveyResponseFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeWithdrawalReason;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.WithdrawnEnrollee;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.WithdrawnEnrolleeService;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
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

    /** test merge of two participants who have enrolled in a combination of studies within a portal */
    @Test
    @Transactional
    public void testMultiStudyMerge(TestInfo info) {
        // build 3 studies in the same portal
        StudyEnvironmentBundle studyEnvBundle1 = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        StudyEnvironmentBundle studyEnvBundle2 = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox, studyEnvBundle1.getPortal(), studyEnvBundle1.getPortalEnv());
        StudyEnvironmentBundle studyEnvBundle3 = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox, studyEnvBundle1.getPortal(), studyEnvBundle1.getPortalEnv());

        // a person created two accounts.  They enrolled in study 1 under both, but only one in study 2 and the other one in study 3
        RegistrationService.RegistrationResult sourceUser = registrationService.register(studyEnvBundle1.getPortal().getShortcode(), studyEnvBundle1.getStudyEnv().getEnvironmentName(),
                "username1", null, null);
        RegistrationService.RegistrationResult targetUser = registrationService.register(studyEnvBundle1.getPortal().getShortcode(), studyEnvBundle1.getStudyEnv().getEnvironmentName(),
                "username2", null, null);

        HubResponse sourceEnrollee1 = enrollmentService.enroll(studyEnvBundle1.getStudyEnv().getEnvironmentName(), studyEnvBundle1.getStudy().getShortcode(), sourceUser.participantUser(), sourceUser.portalParticipantUser(), null);
        HubResponse targetEnrollee1 = enrollmentService.enroll(studyEnvBundle2.getStudyEnv().getEnvironmentName(), studyEnvBundle1.getStudy().getShortcode(), targetUser.participantUser(), targetUser.portalParticipantUser(), null);
        HubResponse sourceEnrollee2 = enrollmentService.enroll(studyEnvBundle1.getStudyEnv().getEnvironmentName(), studyEnvBundle2.getStudy().getShortcode(), sourceUser.participantUser(), sourceUser.portalParticipantUser(), null);
        HubResponse targetEnrollee3 = enrollmentService.enroll(studyEnvBundle3.getStudyEnv().getEnvironmentName(), studyEnvBundle3.getStudy().getShortcode(), targetUser.participantUser(), targetUser.portalParticipantUser(), null);

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

        List<ParticipantTask> sourceTasks = participantTaskService.findByEnrolleeId(sourceBundle.enrollee().getId());
        List<ParticipantTask> targetTasks = participantTaskService.findByEnrolleeId(targetBundle.enrollee().getId());

        // survey1 has a response from both, survey2 has a response from the source, survey 3 has a response from the target
        SurveyResponse responseSt1 = surveyResponseFactory.submitStringAnswer(
                sourceTasks.stream().filter(t -> t.getTargetStableId().equals(survey1.getStableId())).findFirst().orElseThrow(),
                "question1", "source1", true, sourceBundle, studyEnvBundle.getPortal())
                .getResponse();
        SurveyResponse responseTt1 = surveyResponseFactory.submitStringAnswer(
                targetTasks.stream().filter(t -> t.getTargetStableId().equals(survey1.getStableId())).findFirst().orElseThrow(),
                "question1", "target1", true, targetBundle, studyEnvBundle.getPortal())
                .getResponse();
        SurveyResponse responseSt2 = surveyResponseFactory.submitStringAnswer(
                sourceTasks.stream().filter(t -> t.getTargetStableId().equals(survey2.getStableId())).findFirst().orElseThrow(),
                "question1", "source2", true, sourceBundle, studyEnvBundle.getPortal())
                .getResponse();
        SurveyResponse response_Tt3 = surveyResponseFactory.submitStringAnswer(
                targetTasks.stream().filter(t -> t.getTargetStableId().equals(survey3.getStableId())).findFirst().orElseThrow(),
                "question1", "target3", true, targetBundle, studyEnvBundle.getPortal())
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
}
