package bio.terra.pearl.core.service.search.expressions;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.kit.KitRequestFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.ParticipantTaskFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.factory.survey.SurveyResponseFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.address.MailingAddress;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.kit.pepper.PepperKitStatus;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpression;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnrolleeSearchExpressionTest extends BaseSpringBootTest {

    @Autowired
    EnrolleeFactory enrolleeFactory;

    @Autowired
    SurveyFactory surveyFactory;

    @Autowired
    StudyEnvironmentFactory studyEnvironmentFactory;

    @Autowired
    SurveyResponseFactory surveyResponseFactory;

    @Autowired
    EnrolleeSearchExpressionParser enrolleeSearchExpressionParser;

    @Autowired
    ParticipantTaskFactory participantTaskFactory;

    @Autowired
    KitRequestFactory kitRequestFactory;

    @Test
    @Transactional
    public void testBasicEvaluate() {
        String rule = "{profile.givenName} = 'John'";
        EnrolleeSearchExpression searchExp = enrolleeSearchExpressionParser.parseRule(rule);

        assertFalse(searchExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(Enrollee.builder().build())
                .profile(Profile.builder().build())
                .build()));

        assertTrue(searchExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(Enrollee.builder().build())
                .profile(Profile.builder().givenName("John").build())
                .build()));

        assertFalse(searchExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(Enrollee.builder().build())
                .profile(Profile.builder().givenName("Jonas").build())
                .build()));
    }

    @Test
    @Transactional
    public void testBooleanOperators() {
        String andRule = "{profile.givenName} = 'John' and {profile.familyName} = 'Doe'";
        String orRule = "{profile.givenName} = 'John' or {profile.familyName} = 'Doe'";
        EnrolleeSearchExpression andExp = enrolleeSearchExpressionParser.parseRule(andRule);
        EnrolleeSearchExpression orExp = enrolleeSearchExpressionParser.parseRule(orRule);

        assertFalse(andExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(Enrollee.builder().build())
                .profile(Profile.builder().build())
                .build()));

        assertFalse(orExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(Enrollee.builder().build())
                .profile(Profile.builder().build())
                .build()));

        assertFalse(andExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(Enrollee.builder().build())
                .profile(Profile.builder().givenName("John").build())
                .build()));

        assertTrue(orExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(Enrollee.builder().build())
                .profile(Profile.builder().givenName("John").build())
                .build()));

        assertTrue(andExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(Enrollee.builder().build())
                .profile(Profile.builder().givenName("John").familyName("Doe").build())
                .build()));

        assertTrue(orExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(Enrollee.builder().build())
                .profile(Profile.builder().givenName("John").familyName("Doe").build())
                .build()));

        assertTrue(orExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(Enrollee.builder().build())
                .profile(Profile.builder().givenName("Something Else").familyName("Doe").build())
                .build()));
    }

    @Test
    @Transactional
    public void testAnswerEvaluate(TestInfo info) {
        StudyEnvironment studyEnvironment = studyEnvironmentFactory.buildPersisted(getTestName(info));

        Survey survey = surveyFactory.buildPersisted(getTestName(info));
        surveyFactory.attachToEnv(survey, studyEnvironment.getId(), true);

        Enrollee enrolleeMatches = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        surveyResponseFactory.buildWithAnswers(enrolleeMatches, survey, Map.of(
                "oh_oh_givenName", "John"
        ));
        Enrollee enrolleeDoesNotMatch = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        surveyResponseFactory.buildWithAnswers(enrolleeDoesNotMatch, survey, Map.of(
                "oh_oh_givenName", "jonas"
        ));
        Enrollee enrolleeNoResponse = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);


        String rule = "{answer.%s.oh_oh_givenName} = 'John'".formatted(survey.getStableId());
        EnrolleeSearchExpression searchExp = enrolleeSearchExpressionParser.parseRule(rule);

        assertTrue(searchExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(enrolleeMatches)
                .build()));

        assertFalse(searchExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(enrolleeDoesNotMatch)
                .build()));

        assertFalse(searchExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(enrolleeNoResponse)
                .build()));
    }


    @Test
    @Transactional
    public void testEvaluateMailingAddress() {
        String rule = "{profile.mailingAddress.state} = 'MA'";
        EnrolleeSearchExpression searchExp = enrolleeSearchExpressionParser.parseRule(rule);

        assertFalse(searchExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(Enrollee.builder().build())
                .profile(Profile.builder().build())
                .build()));

        assertTrue(searchExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(Enrollee.builder().build())
                .profile(Profile.builder().mailingAddress(MailingAddress.builder().state("MA").build()).build())
                .build()));

        assertFalse(searchExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(Enrollee.builder().build())
                .profile(Profile.builder().mailingAddress(MailingAddress.builder().state("NY").build()).build())
                .build()));
    }

    @Test
    @Transactional
    public void testEvaluateLatestKit(TestInfo info) throws Exception {
        String latestKitCreated = "{latestKit.status} = 'CREATED'";
        String latestKitErrored = "{latestKit.status} = 'ERRORED'";

        EnrolleeSearchExpression latestKitCreatedExp = enrolleeSearchExpressionParser.parseRule(latestKitCreated);
        EnrolleeSearchExpression latestKitErroredExp = enrolleeSearchExpressionParser.parseRule(latestKitErrored);

        Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info));

        kitRequestFactory.buildPersisted(
                getTestName(info),
                enrollee,
                PepperKitStatus.CREATED);

        assertTrue(latestKitCreatedExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrollee).build()));
        assertFalse(latestKitErroredExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrollee).build()));

        kitRequestFactory.buildPersisted(
                getTestName(info),
                enrollee,
                PepperKitStatus.ERRORED);

        assertFalse(latestKitCreatedExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrollee).build()));
        assertTrue(latestKitErroredExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrollee).build()));
    }

    @Test
    public void testParenthesis() {
        assertTrue(
                enrolleeSearchExpressionParser
                        .parseRule("1 = 1 and (1 = 1 or 1 = 2)")
                        .evaluate(EnrolleeSearchContext.builder().build())
        );

        assertTrue(
                enrolleeSearchExpressionParser
                        .parseRule("1 = 1 and (1 = 2 or (1 = 2 or (1 = 1 and 2 = 2)))")
                        .evaluate(EnrolleeSearchContext.builder().build())
        );
    }

    @Test
    @Transactional
    public void testAnswersWithParensEvaluate(TestInfo info) {
        StudyEnvironment studyEnvironment = studyEnvironmentFactory.buildPersisted(getTestName(info));

        Survey survey = surveyFactory.buildPersisted(getTestName(info));
        surveyFactory.attachToEnv(survey, studyEnvironment.getId(), true);

        Enrollee enrolleeMatches1 = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        surveyResponseFactory.buildWithAnswers(enrolleeMatches1, survey, Map.of(
                "diagnosis", "diag1",
                "country", "us"

        ));

        Enrollee enrolleeMatches2 = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        surveyResponseFactory.buildWithAnswers(enrolleeMatches2, survey, Map.of(
                "diagnosis", "diag2",
                "country", "us"
        ));

        Enrollee enrolleeDoesNotMatch1 = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        surveyResponseFactory.buildWithAnswers(enrolleeDoesNotMatch1, survey, Map.of(
                "diagnosis", "diag3",
                "country", "us"
        ));

        Enrollee enrolleeDoesNotMatch2 = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        surveyResponseFactory.buildWithAnswers(enrolleeDoesNotMatch2, survey, Map.of(
                "diagnosis", "diag2",
                "country", "gb"
        ));

        Enrollee enrolleeNoResponse = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);

        String rule = "{answer.%s.country} = 'us' and ({answer.%s.diagnosis} = 'diag1' or {answer.%s.diagnosis} = 'diag2')".formatted(survey.getStableId(), survey.getStableId(), survey.getStableId());
        EnrolleeSearchExpression searchExp = enrolleeSearchExpressionParser.parseRule(rule);

        assertTrue(searchExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(enrolleeMatches1)
                .build()));

        assertTrue(searchExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(enrolleeMatches1)
                .build()));

        assertFalse(searchExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(enrolleeDoesNotMatch1)
                .build()));

        assertFalse(searchExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(enrolleeDoesNotMatch2)
                .build()));

        assertFalse(searchExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(enrolleeNoResponse)
                .build()));
    }

    @Test
    public void testTaskEvaluate(TestInfo info) {
        StudyEnvironmentFactory.StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        EnrolleeSearchExpression assignedExp = enrolleeSearchExpressionParser.parseRule(
                "{task.demographic_survey.assigned} = true"
        );

        EnrolleeSearchExpression inProgressExp = enrolleeSearchExpressionParser.parseRule(
                "{task.demographic_survey.status} = 'IN_PROGRESS'"
        );

        // enrollee not assigned
        EnrolleeFactory.EnrolleeBundle eBundleNotAssigned = enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());
        Enrollee enrolleeNotAssigned = eBundleNotAssigned.enrollee();

        // enrollee assigned not started
        EnrolleeFactory.EnrolleeBundle eBundleNotStarted = enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());
        participantTaskFactory.buildPersisted(eBundleNotStarted, "demographic_survey", TaskStatus.NEW, TaskType.SURVEY);
        Enrollee enrolleeNotStarted = eBundleNotStarted.enrollee();

        // enrollee assigned in progress
        EnrolleeFactory.EnrolleeBundle eBundleInProgress = enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());
        participantTaskFactory.buildPersisted(eBundleInProgress, "demographic_survey", TaskStatus.IN_PROGRESS, TaskType.SURVEY);
        Enrollee enrolleeInProgress = eBundleInProgress.enrollee();

        // enrollee assigned in progress but different task
        EnrolleeFactory.EnrolleeBundle eBundleInProgressWrongTask = enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());
        participantTaskFactory.buildPersisted(eBundleInProgressWrongTask, "something_else", TaskStatus.IN_PROGRESS, TaskType.SURVEY);
        Enrollee enrolleeInProgressWrongTask = eBundleInProgressWrongTask.enrollee();

        assertTrue(assignedExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrolleeNotStarted).build()));
        assertTrue(assignedExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrolleeInProgress).build()));
        assertFalse(assignedExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrolleeNotAssigned).build()));
        assertFalse(assignedExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrolleeInProgressWrongTask).build()));

        assertTrue(inProgressExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrolleeInProgress).build()));
        assertFalse(inProgressExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrolleeNotStarted).build()));
        assertFalse(inProgressExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrolleeNotAssigned).build()));
        assertFalse(inProgressExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrolleeInProgressWrongTask).build()));
    }

    @Test
    public void testEnrolleeFields() {
        assertTrue(enrolleeSearchExpressionParser
                .parseRule("{enrollee.consented} = true")
                .evaluate(
                        EnrolleeSearchContext
                                .builder()
                                .enrollee(Enrollee.builder()
                                        .consented(true)
                                        .build())
                                .build()));

        assertFalse(enrolleeSearchExpressionParser
                .parseRule("{enrollee.consented} = false")
                .evaluate(
                        EnrolleeSearchContext
                                .builder()
                                .enrollee(Enrollee.builder()
                                        .consented(true)
                                        .build())
                                .build()));

        assertTrue(enrolleeSearchExpressionParser
                .parseRule("{enrollee.subject} = true")
                .evaluate(
                        EnrolleeSearchContext
                                .builder()
                                .enrollee(Enrollee.builder()
                                        .subject(true)
                                        .build())
                                .build()));

        assertFalse(enrolleeSearchExpressionParser
                .parseRule("{enrollee.subject} = false")
                .evaluate(
                        EnrolleeSearchContext
                                .builder()
                                .enrollee(Enrollee.builder()
                                        .subject(true)
                                        .build())
                                .build()));

        assertTrue(enrolleeSearchExpressionParser
                .parseRule("{enrollee.shortcode} = 'JSALK'")
                .evaluate(
                        EnrolleeSearchContext
                                .builder()
                                .enrollee(Enrollee.builder()
                                        .shortcode("JSALK")
                                        .build())
                                .build()));
    }

    @Test
    public void testContains() {
        assertTrue(enrolleeSearchExpressionParser
                .parseRule("{profile.name} contains 'Jonas Salk'")
                .evaluate(
                        EnrolleeSearchContext
                                .builder()
                                .profile(Profile.builder()
                                        .givenName("Jonas")
                                        .familyName("Salk")
                                        .build())
                                .build()));

        // case insensitive
        assertTrue(enrolleeSearchExpressionParser
                .parseRule("{profile.name} contains 'jonas'")
                .evaluate(
                        EnrolleeSearchContext
                                .builder()
                                .profile(Profile.builder()
                                        .givenName("Jonas")
                                        .familyName("Salk")
                                        .build())
                                .build()));

        assertTrue(enrolleeSearchExpressionParser
                .parseRule("{profile.name} contains 'nas Sa'")
                .evaluate(
                        EnrolleeSearchContext
                                .builder()
                                .profile(Profile.builder()
                                        .givenName("Jonas")
                                        .familyName("Salk")
                                        .build())
                                .build()));

        assertTrue(enrolleeSearchExpressionParser
                .parseRule("{profile.name} contains 'alk'")
                .evaluate(
                        EnrolleeSearchContext
                                .builder()
                                .profile(Profile.builder()
                                        .givenName("Jonas")
                                        .familyName("Salk")
                                        .build())
                                .build()));

        assertFalse(enrolleeSearchExpressionParser
                .parseRule("{profile.name} contains 'Jonas Sa'")
                .evaluate(
                        EnrolleeSearchContext
                                .builder()
                                .profile(Profile.builder()
                                        .givenName("Jonas")
                                        .familyName("Balk")
                                        .build())
                                .build()));

        assertFalse(enrolleeSearchExpressionParser
                .parseRule("{profile.name} contains 'John Salk'")
                .evaluate(
                        EnrolleeSearchContext
                                .builder()
                                .profile(Profile.builder()
                                        .givenName("Jonas")
                                        .familyName("Salk")
                                        .build())
                                .build()));

        assertFalse(enrolleeSearchExpressionParser
                .parseRule("{profile.name} contains 'null'")
                .evaluate(
                        EnrolleeSearchContext
                                .builder()
                                .profile(Profile.builder()
                                        .build())
                                .build()));

    }

}
