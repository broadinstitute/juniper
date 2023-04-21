package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.participant.PortalParticipantUserFactory;
import bio.terra.pearl.core.factory.survey.AnswerFactory;
import bio.terra.pearl.core.factory.survey.SurveyResponseFactory;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.survey.*;
import bio.terra.pearl.core.model.workflow.DataChangeRecord;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.workflow.DataChangeRecordService;
import java.util.List;
import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

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
    private AnswerService answerService;
    @Autowired
    private StudyEnvironmentSurveyService studyEnvironmentSurveyService;
    @Autowired
    private PortalParticipantUserFactory portalParticipantUserFactory;
    @Autowired
    private DataChangeRecordService dataChangeRecordService;

    @Test
    @Transactional
    public void testSurveyResponseCrud() {
        SurveyResponse surveyResponse = surveyResponseFactory.builderWithDependencies("testSurveyResponseCrud")
                .build();
        SurveyResponse savedResponse = surveyResponseService.create(surveyResponse);
        DaoTestUtils.assertGeneratedProperties(savedResponse);
        Assertions.assertEquals(surveyResponse.getSurveyId(), savedResponse.getSurveyId());
    }

    @Test
    @Transactional
    public void testSurveyResponseWithAnswers() {
        String testName = "testSurveyResponseCrud";
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
                survey.getStableId(), survey.getVersion(), enrollee, null);
        assertThat(survWithResponse, notNullValue());
        assertThat(survWithResponse.surveyResponse(), notNullValue());
        assertThat(survWithResponse.surveyResponse().getAnswers(), hasSize(2));
        assertThat(survWithResponse.studyEnvironmentSurvey().getSurveyOrder(), equalTo(2));
        assertThat(survWithResponse.studyEnvironmentSurvey().getSurvey().getId(),
                equalTo(survey.getId()));
    }

    @Test
    @Transactional
    public void testSurveyResponseUpdateAnswers() {
        String testName = "testSurveyResponseCrud";
        List<Answer> answers = AnswerFactory.fromMap(Map.of("foo", "bar", "test1", "ans1"));
        SurveyResponse surveyResponse = surveyResponseFactory.builderWithDependencies(testName)
                .answers(answers)
                .build();
        Survey survey = surveyService.find(surveyResponse.getSurveyId()).get();
        SurveyResponse savedResponse = surveyResponseService.create(surveyResponse);
        PortalParticipantUser ppUser = portalParticipantUserFactory
                .buildPersisted("testSurveyResponseUpdateAnswers", savedResponse.getEnrolleeId());

        List<Answer> updatedAnswers = AnswerFactory.fromMap(Map.of("foo", "baz", "q3", "answer3"));
        surveyResponseService.createOrUpdateAnswers(updatedAnswers, savedResponse, survey, ppUser);
        for (Answer updatedAnswer : updatedAnswers) {
            Answer savedAnswer = answerService.findForQuestion(savedResponse.getId(), updatedAnswer.getQuestionStableId()).get();
            assertThat(savedAnswer.getStringValue(), equalTo(updatedAnswer.getStringValue()));
        }
        assertThat(answerService.findAll(savedResponse.getId(), List.of("foo", "q3", "test1")), hasSize(3));

        List<DataChangeRecord> changeRecords = dataChangeRecordService.findByEnrollee(savedResponse.getEnrolleeId());
        assertThat(changeRecords.size(), equalTo(1));
        assertThat(changeRecords.get(0), samePropertyValuesAs(DataChangeRecord.builder()
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
        surveyResponseService.createOrUpdateAnswers(List.of(nullAnswer), savedResponse, survey, ppUser);
        Answer savedAnswer = answerService.findForQuestion(savedResponse.getId(), "q3").get();
        assertThat(savedAnswer.getStringValue(), nullValue());
        changeRecords = dataChangeRecordService.findByEnrollee(savedResponse.getEnrolleeId());
        assertThat(changeRecords.size(), equalTo(2));
    }


}
