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
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.WithdrawnEnrollee;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.WithdrawnEnrolleeService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import com.google.api.client.util.Objects;
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
    private ParticipantUserService participantUserService;
    @Autowired
    private ParticipantTaskService participantTaskService;
    @Autowired
    private WithdrawnEnrolleeService withdrawnEnrolleeService;
    @Autowired
    private SurveyResponseFactory surveyResponseFactory;

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
        List<ParticipantUser> allUsers = participantUserService.findAll();
        assertThat(allUsers, hasSize(1));
        assertThat(allUsers.get(0).getId(), equalTo(targetBundle.participantUser().getId()));

        // confirm the source enrollee is withdrawn
        List<WithdrawnEnrollee> withdrawnUsers = withdrawnEnrolleeService.findByStudyEnvironmentIdNoData(studyEnvBundle.getStudyEnv().getId());
        assertThat(withdrawnUsers, hasSize(1));
        assertThat(withdrawnUsers.get(0).getShortcode(), equalTo(sourceBundle.enrollee().getShortcode()));
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
        surveyResponseFactory.submitStringAnswer(
                sourceTasks.stream().filter(t -> t.getTargetStableId().equals(survey1.getStableId())).findFirst().orElseThrow(),
                "question1", "source1", true, sourceBundle, studyEnvBundle.getPortal());
        surveyResponseFactory.submitStringAnswer(
                targetTasks.stream().filter(t -> t.getTargetStableId().equals(survey1.getStableId())).findFirst().orElseThrow(),
                "question1", "target1", true, targetBundle, studyEnvBundle.getPortal());
        surveyResponseFactory.submitStringAnswer(
                sourceTasks.stream().filter(t -> t.getTargetStableId().equals(survey2.getStableId())).findFirst().orElseThrow(),
                "question1", "source2", true, sourceBundle, studyEnvBundle.getPortal());
        surveyResponseFactory.submitStringAnswer(
                targetTasks.stream().filter(t -> t.getTargetStableId().equals(survey3.getStableId())).findFirst().orElseThrow(),
                "question1", "target3", true, targetBundle, studyEnvBundle.getPortal());

        ParticipantUserMerge merge = participantMergePlanService.planMerge(sourceBundle.participantUser(), targetBundle.participantUser(),
                studyEnvBundle.getPortal());

        assertThat(merge.getEnrollees(), hasSize(1));
        List<MergeAction<ParticipantTask, ?>> taskMerges = merge.getEnrollees().get(0).getMergePlan().getTasks();
        // there should be 3 task 'pairs'
        assertThat(taskMerges, hasSize(3));
        for (MergeAction<ParticipantTask, ?> taskMerge : taskMerges) {
            if (Objects.equal(taskMerge.getSource().getTargetStableId(), survey1.getStableId())) {
                assertThat(taskMerge.getTarget().getTargetStableId(), equalTo(survey1.getStableId()));
                assertThat(taskMerge.getAction(), equalTo(MergeAction.Action.MERGE));
            } else if (Objects.equal(taskMerge.getSource().getTargetStableId(), survey2.getStableId())) {
                assertThat(taskMerge.getTarget().getTargetStableId(), equalTo(survey2.getStableId()));
                assertThat(taskMerge.getAction(), equalTo(MergeAction.Action.DELETE_TARGET));
            } else if (Objects.equal(taskMerge.getTarget().getTargetStableId(), survey3.getStableId())) {
                assertThat(taskMerge.getSource().getTargetStableId(), equalTo(survey3.getStableId()));
                assertThat(taskMerge.getAction(), equalTo(MergeAction.Action.DELETE_SOURCE));
            } else {
                throw new RuntimeException("unexpected task merge");
            }
        }
    }
}
