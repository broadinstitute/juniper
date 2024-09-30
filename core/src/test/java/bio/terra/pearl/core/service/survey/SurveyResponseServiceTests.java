package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.PortalParticipantUserFactory;
import bio.terra.pearl.core.factory.survey.AnswerFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.factory.survey.SurveyResponseFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.audit.ParticipantDataChange;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.survey.*;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.workflow.ParticipantDataChangeService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SurveyResponseServiceTests extends BaseSpringBootTest {
    @Autowired
    private SurveyResponseFactory surveyResponseFactory;
    @Autowired
    private SurveyResponseService surveyResponseService;
    @Autowired
    private EnrolleeService enrolleeService;
    @Autowired
    private SurveyService surveyService;
    @Autowired
    private SurveyFactory surveyFactory;
    @Autowired
    private AnswerService answerService;
    @Autowired
    private ParticipantUserService participantUserService;
    @Autowired
    private StudyEnvironmentSurveyService studyEnvironmentSurveyService;
    @Autowired
    private PortalParticipantUserFactory portalParticipantUserFactory;
    @Autowired
    private ParticipantDataChangeService participantDataChangeService;
    @Autowired
    private ParticipantTaskService participantTaskService;
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private SurveyTaskDispatcher surveyTaskDispatcher;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;

    @Test
    @Transactional
    public void testSurveyResponseCrud(TestInfo testInfo) {
        SurveyResponse surveyResponse = surveyResponseFactory.builderWithDependencies(getTestName(testInfo))
                .build();
        SurveyResponse savedResponse = surveyResponseService.create(surveyResponse);
        DaoTestUtils.assertGeneratedProperties(savedResponse);
        assertEquals(surveyResponse.getSurveyId(), savedResponse.getSurveyId());
    }

    @Test
    @Transactional
    public void testSurveyResponseWithAnswers(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        List<Answer> answers = AnswerFactory.fromMap(Map.of("foo", "bar", "test1", "ans1"));
        SurveyResponse surveyResponse = surveyResponseFactory.builderWithDependencies(testName)
                .answers(answers)
                .build();

        SurveyResponse savedResponse = surveyResponseService.create(surveyResponse);
        DaoTestUtils.assertGeneratedProperties(savedResponse);
        assertThat(savedResponse.getAnswers(), hasSize(2));
        Answer fooAnswer = savedResponse.getAnswers().stream()
                .filter(ans -> ans.getQuestionStableId().equals("foo")).findFirst().get();
        assertThat(fooAnswer.getAnswerType(), equalTo(AnswerType.STRING));
        assertThat(fooAnswer.getEnrolleeId(), equalTo(surveyResponse.getEnrolleeId()));
        assertThat(fooAnswer.getSurveyResponseId(), equalTo(savedResponse.getId()));
        assertThat(fooAnswer.getStringValue(), equalTo("bar"));
        assertThat(fooAnswer.getCreatingParticipantUserId(), equalTo(surveyResponse.getCreatingParticipantUserId()));

        // attach the form to a study environment so we can test full retrieval
        Enrollee enrollee = enrolleeService.find(savedResponse.getEnrolleeId()).get();
        Survey survey = surveyService.find(savedResponse.getSurveyId()).get();
        studyEnvironmentSurveyService.create(StudyEnvironmentSurvey.builder()
                .surveyId(survey.getId())
                .studyEnvironmentId(enrollee.getStudyEnvironmentId())
                .surveyOrder(2)
                .build());

        SurveyWithResponse survWithResponse = surveyResponseService.findWithActiveResponse(enrollee.getStudyEnvironmentId(),
                survey.getPortalId(), survey.getStableId(), survey.getVersion(), enrollee, null);
        assertThat(survWithResponse, notNullValue());
        assertThat(survWithResponse.surveyResponse(), notNullValue());
        assertThat(survWithResponse.surveyResponse().getAnswers(), hasSize(2));
        assertThat(survWithResponse.studyEnvironmentSurvey().getSurveyOrder(), equalTo(2));
        assertThat(survWithResponse.studyEnvironmentSurvey().getSurvey().getId(),
                equalTo(survey.getId()));
    }

    @Test
    @Transactional
    public void testSurveyResponseWithAnswersAttachesReferencesAnswers(TestInfo info) {
        StudyEnvironmentFactory.StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);

        Survey survey1 = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(info))
                .portalId(studyEnvBundle.getPortal().getId())
                .stableId("survey1")
                .content("{\"pages\":[{\"elements\":[{\"type\":\"text\",\"name\":\"diagnosis\",\"title\":\"What is your diagnosis?\"}]}]}"));

        surveyFactory.attachToEnv(survey1, studyEnvBundle.getStudyEnv().getId(), true);

        Survey survey2 = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(info))
                .portalId(studyEnvBundle.getPortal().getId())
                .stableId("survey2")
                .content("{\"pages\":[{\"elements\":[{\"type\":\"text\",\"name\":\"diagnosis\",\"title\":\"Tell me more about {survey1.diagnosis}\"}]}]}"));

        surveyFactory.attachToEnv(survey2, studyEnvBundle.getStudyEnv().getId(), true);


        Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info), studyEnvBundle.getStudyEnv());

        surveyResponseFactory.buildWithAnswers(enrollee, survey1, Map.of("diagnosis", "old response, should ignore"));
        surveyResponseFactory.buildWithAnswers(enrollee, survey1, Map.of("diagnosis", "cancer"));
        
        SurveyWithResponse surveyWithResponse = surveyResponseService.findWithActiveResponse(studyEnvBundle.getStudyEnv().getId(),
                studyEnvBundle.getPortal().getId(), survey2.getStableId(), survey2.getVersion(), enrollee, null);

        assertEquals(1, surveyWithResponse.referencedAnswers().size());

        Answer answer = surveyWithResponse.referencedAnswers().get(0);

        assertEquals(answer.getSurveyStableId(), "survey1");
        assertEquals(answer.getQuestionStableId(), "diagnosis");
        assertEquals(answer.getStringValue(), "cancer");

    }

    @Test
    @Transactional
    public void testSurveyResponseUpdateAnswers(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        List<Answer> answers = AnswerFactory.fromMap(Map.of("foo", "bar", "test1", "ans1"));
        SurveyResponse surveyResponse = surveyResponseFactory.builderWithDependencies(testName)
                .answers(answers)
                .build();
        Survey survey = surveyService.find(surveyResponse.getSurveyId()).get();
        SurveyResponse savedResponse = surveyResponseService.create(surveyResponse);
        PortalParticipantUser ppUser = portalParticipantUserFactory
                .buildPersisted(getTestName(testInfo), savedResponse.getEnrolleeId());
        ParticipantUser pUser = participantUserService.find(ppUser.getParticipantUserId()).get();

        List<Answer> updatedAnswers = AnswerFactory.fromMap(Map.of("foo", "baz", "q3", "answer3"));
        surveyResponseService.createOrUpdateAnswers(updatedAnswers, savedResponse, null, survey, ppUser, new ResponsibleEntity(pUser));
        for (Answer updatedAnswer : updatedAnswers) {
            Answer savedAnswer = answerService.findForQuestion(savedResponse.getId(), updatedAnswer.getQuestionStableId()).get();
            assertThat(savedAnswer.getStringValue(), equalTo(updatedAnswer.getStringValue()));
            assertThat(savedAnswer.getAnswerType(), equalTo(AnswerType.STRING));
        }
        assertThat(answerService.findByResponseAndQuestions(savedResponse.getId(), List.of("foo", "q3", "test1")), hasSize(3));

        List<ParticipantDataChange> changeRecords = participantDataChangeService.findByEnrollee(savedResponse.getEnrolleeId());
        assertThat(changeRecords.size(), equalTo(1));
        assertThat(changeRecords.get(0), samePropertyValuesAs(ParticipantDataChange.builder()
                        .enrolleeId(savedResponse.getEnrolleeId())
                        .surveyId(survey.getId())
                        .responsibleUserId(ppUser.getParticipantUserId())
                        .portalParticipantUserId(ppUser.getId())
                        .operationId(savedResponse.getId())
                        .modelName(survey.getStableId())
                        .fieldName("foo")
                        .oldValue("bar")
                        .newValue("baz").build(),
                "id", "createdAt", "lastUpdatedAt"));

        Answer nullAnswer = Answer.builder().questionStableId("q3").stringValue(null).build();
        surveyResponseService.createOrUpdateAnswers(List.of(nullAnswer), savedResponse, null, survey, ppUser, new ResponsibleEntity(pUser));
        Answer savedAnswer = answerService.findForQuestion(savedResponse.getId(), "q3").get();
        assertThat(savedAnswer.getStringValue(), nullValue());
        changeRecords = participantDataChangeService.findByEnrollee(savedResponse.getEnrolleeId());
        assertThat(changeRecords.size(), equalTo(2));
    }


    @Test
    @Transactional
    public void testUpdateResponse(TestInfo testInfo) {
        // create a survey and an enrollee with one task to complete that survey
        String testName = getTestName(testInfo);
        EnrolleeFactory.EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(testName);
        Survey survey = surveyFactory.buildPersisted(testName);
        StudyEnvironmentSurvey configuredSurvey = surveyFactory.attachToEnv(survey, enrolleeBundle.enrollee().getStudyEnvironmentId(), true);

        ParticipantTask task = surveyTaskDispatcher.buildTask(enrolleeBundle.enrollee(), enrolleeBundle.portalParticipantUser(), configuredSurvey, survey);
        task = participantTaskService.create(task, getAuditInfo(testInfo));
        assertThat(task.getStatus(), equalTo(TaskStatus.NEW));

        // create a response with no answers
        SurveyResponse response = SurveyResponse.builder()
                .enrolleeId(enrolleeBundle.enrollee().getId())
                .creatingParticipantUserId(enrolleeBundle.enrollee().getParticipantUserId())
                .surveyId(survey.getId())
                .complete(false)
                .answers(List.of())
                .build();

        surveyResponseService.updateResponse(response, new ResponsibleEntity(enrolleeBundle.participantUser()), null,
                enrolleeBundle.portalParticipantUser(), enrolleeBundle.enrollee(), task.getId(), survey.getPortalId());

        // check that the response was created and task status updated to viewed
        task = participantTaskService.find(task.getId()).orElseThrow();
        assertThat(task.getStatus(), equalTo(TaskStatus.VIEWED));

        // now an updated response with one Answer
        List<Answer> updatedAnswers = AnswerFactory.fromMap(Map.of("foo", "baz", "q3", "answer3"));
        response = SurveyResponse.builder()
                .enrolleeId(enrolleeBundle.enrollee().getId())
                .creatingParticipantUserId(enrolleeBundle.enrollee().getParticipantUserId())
                .surveyId(survey.getId())
                .complete(false)
                .answers(updatedAnswers)
                .build();

        surveyResponseService.updateResponse(response, new ResponsibleEntity(enrolleeBundle.participantUser()), null,
                enrolleeBundle.portalParticipantUser(), enrolleeBundle.enrollee(), task.getId(), survey.getPortalId());

        // check that the response was created and task status updated to viewed
        task = participantTaskService.find(task.getId()).orElseThrow();
        assertThat(task.getStatus(), equalTo(TaskStatus.IN_PROGRESS));
        // check that the answers were created
        SurveyResponse savedResponse = surveyResponseService.findByEnrolleeId(enrolleeBundle.enrollee().getId()).get(0);
        assertThat(answerService.findByResponse(savedResponse.getId()), hasSize(2));
    }

    @Test
    @Transactional
    public void testCompletedSurveyResponseCannotBeUpdatedToIncomplete(TestInfo testInfo) {
        // create a survey and an enrollee with one survey task
        String testName = getTestName(testInfo);
        EnrolleeFactory.EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(testName);
        Survey survey = surveyFactory.buildPersisted(testName);
        StudyEnvironmentSurvey configuredSurvey = surveyFactory.attachToEnv(survey, enrolleeBundle.enrollee().getStudyEnvironmentId(), true);

        ParticipantTask task = surveyTaskDispatcher.buildTask(enrolleeBundle.enrollee(), enrolleeBundle.portalParticipantUser(), configuredSurvey, survey);
        task = participantTaskService.create(task, getAuditInfo(testInfo));
        assertThat(task.getStatus(), equalTo(TaskStatus.NEW));

        // create a response complete flag set to true
        SurveyResponse response = SurveyResponse.builder()
                .enrolleeId(enrolleeBundle.enrollee().getId())
                .creatingParticipantUserId(enrolleeBundle.enrollee().getParticipantUserId())
                .surveyId(survey.getId())
                .complete(true) // set the response to complete
                .answers(List.of())
                .build();

        surveyResponseService.updateResponse(response, new ResponsibleEntity(enrolleeBundle.participantUser()), null,
                enrolleeBundle.portalParticipantUser(), enrolleeBundle.enrollee(), task.getId(), survey.getPortalId());

        // check that the task response was created and task status updated to complete
        task = participantTaskService.find(task.getId()).orElseThrow();
        assertThat(task.getStatus(), equalTo(TaskStatus.COMPLETE));

        // check that the SurveyResponse is marked as complete
        SurveyResponse savedResponse = surveyResponseService.findByEnrolleeId(enrolleeBundle.enrollee().getId()).get(0);
        assertThat(savedResponse.isComplete(), equalTo(true));

        // create a response with complete flag set to false
        response = SurveyResponse.builder()
                .enrolleeId(enrolleeBundle.enrollee().getId())
                .creatingParticipantUserId(enrolleeBundle.enrollee().getParticipantUserId())
                .surveyId(survey.getId())
                .complete(false) // set the response to incomplete
                .answers(List.of())
                .build();

        surveyResponseService.updateResponse(response, new ResponsibleEntity(enrolleeBundle.participantUser()), null,
                enrolleeBundle.portalParticipantUser(), enrolleeBundle.enrollee(), task.getId(), survey.getPortalId());

        // check that the task status remains complete
        task = participantTaskService.find(task.getId()).orElseThrow();
        assertThat(task.getStatus(), equalTo(TaskStatus.COMPLETE));

        // check that the SurveyResponse is still marked as complete
        savedResponse = surveyResponseService.findByEnrolleeId(enrolleeBundle.enrollee().getId()).get(0);
        assertThat(savedResponse.isComplete(), equalTo(true));
    }

}
