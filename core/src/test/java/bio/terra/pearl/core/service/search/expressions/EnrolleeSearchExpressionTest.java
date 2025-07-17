package bio.terra.pearl.core.service.search.expressions;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentBundle;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.kit.KitRequestFactory;
import bio.terra.pearl.core.factory.participant.*;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.factory.survey.SurveyResponseFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.address.MailingAddress;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Family;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.kit.pepper.PepperKitStatus;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.rule.EnrolleeContext;
import bio.terra.pearl.core.service.rule.EnrolleeContextService;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpression;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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
    @Autowired
    FamilyFactory familyFactory;
    @Autowired
    EnrolleeContextService enrolleeContextService;
    @Autowired
    PortalParticipantUserService portalParticipantUserService;
    @Autowired
    ParticipantUserService participantUserService;


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
    public void testInstantParseEvaluate(TestInfo info) {
        StudyEnvironment studyEnvironment = studyEnvironmentFactory.buildPersisted(getTestName(info));

        Survey survey = surveyFactory.buildPersisted(getTestName(info));
        surveyFactory.attachToEnv(survey, studyEnvironment.getId(), true);

        Enrollee enrolleeMatches = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        enrolleeMatches.setCreatedAt(Instant.now().minusSeconds(3600));
        Enrollee enrolleeDoesNotMatch = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);

        String dateString = Instant.now().minusSeconds(1800).toString();
        String rule = "{enrollee.createdAt} < '%s'".formatted(dateString);
        EnrolleeSearchExpression searchExp = enrolleeSearchExpressionParser.parseRule(rule);

        assertTrue(searchExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(enrolleeMatches)
                .build()));

        assertFalse(searchExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(enrolleeDoesNotMatch)
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
    public void testEvaluateMailingAddressFromDb(TestInfo info) {
        StudyEnvironment studyEnvironment = studyEnvironmentFactory.buildPersisted(getTestName(info));
        Enrollee maEnrollee = enrolleeFactory.buildPersisted(getTestName(info),
                studyEnvironment,
                Profile.builder().mailingAddress(MailingAddress.builder().state("MA").build()).build());
        EnrolleeContext context = enrolleeContextService.fetchData(maEnrollee);
        EnrolleeSearchContext searchContext = EnrolleeSearchContext.builder()
                .profile(context.getProfile())
                .enrollee(context.getEnrollee()).build();

        EnrolleeSearchExpression searchExp = enrolleeSearchExpressionParser.parseRule("{profile.mailingAddress.state} = 'MA'");
        assertTrue(searchExp.evaluate(searchContext));

        searchExp = enrolleeSearchExpressionParser.parseRule("{profile.mailingAddress.state} = 'CT'");
        assertFalse(searchExp.evaluate(searchContext));
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
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        EnrolleeSearchExpression assignedExp = enrolleeSearchExpressionParser.parseRule(
                "{task.demographic_survey.assigned} = true"
        );

        EnrolleeSearchExpression inProgressExp = enrolleeSearchExpressionParser.parseRule(
                "{task.demographic_survey.status} = 'IN_PROGRESS'"
        );

        // enrollee not assigned
        EnrolleeBundle eBundleNotAssigned = enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());
        Enrollee enrolleeNotAssigned = eBundleNotAssigned.enrollee();

        // enrollee assigned not started
        EnrolleeBundle eBundleNotStarted = enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());
        participantTaskFactory.buildPersisted(eBundleNotStarted, "demographic_survey", TaskStatus.NEW, TaskType.SURVEY);
        Enrollee enrolleeNotStarted = eBundleNotStarted.enrollee();

        // enrollee assigned in progress
        EnrolleeBundle eBundleInProgress = enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());
        participantTaskFactory.buildPersisted(eBundleInProgress, "demographic_survey", TaskStatus.IN_PROGRESS, TaskType.SURVEY);
        Enrollee enrolleeInProgress = eBundleInProgress.enrollee();

        // enrollee assigned in progress but different task
        EnrolleeBundle eBundleInProgressWrongTask = enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());
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
    @Transactional
    public void testPortalUser(TestInfo info) {
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        EnrolleeBundle bundle = enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());
        Instant loginTime = Instant.now();
        bundle.portalParticipantUser().setLastLogin(loginTime);
        portalParticipantUserService.update(bundle.portalParticipantUser());

        assertFalse(enrolleeSearchExpressionParser
                .parseRule("{portalUser.lastLogin} < {user.createdAt}")
                .evaluate(
                        EnrolleeSearchContext
                                .builder()
                                .enrollee(bundle.enrollee())
                                .build()));


        bundle = enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());
        loginTime = Instant.now().minusSeconds(3600);
        bundle.portalParticipantUser().setLastLogin(loginTime);
        portalParticipantUserService.update(bundle.portalParticipantUser());

        assertTrue(enrolleeSearchExpressionParser
                .parseRule("{portalUser.lastLogin} < {user.createdAt}")
                .evaluate(
                        EnrolleeSearchContext
                                .builder()
                                .enrollee(bundle.enrollee())
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


    @Test
    @Transactional
    public void testLowerFunction(TestInfo info) {
        Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info));

        EnrolleeSearchExpression lowerExp = enrolleeSearchExpressionParser.parseRule(
                "lower({enrollee.shortcode}) = '" + enrollee.getShortcode().toLowerCase() + "'"
        );

        assertTrue(lowerExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrollee).build()));
    }

    @Test
    @Transactional
    public void testNot(TestInfo info) {
        EnrolleeAndProxy bundle = enrolleeFactory.buildProxyAndGovernedEnrollee(getTestName(info), "proxy@test.com");

        EnrolleeSearchExpression notSubjectExp = enrolleeSearchExpressionParser.parseRule(
                "!{enrollee.subject} = true"
        );


        // test if it works with parents
        EnrolleeSearchExpression notSubjectOrConsentedExp = enrolleeSearchExpressionParser.parseRule(
                "!({enrollee.subject} = true or {enrollee.consented} = true)"
        );

        assertFalse(notSubjectExp.evaluate(EnrolleeSearchContext.builder().enrollee(bundle.governedEnrollee()).build()));
        assertTrue(notSubjectExp.evaluate(EnrolleeSearchContext.builder().enrollee(bundle.proxy()).build()));

        assertFalse(notSubjectOrConsentedExp.evaluate(EnrolleeSearchContext.builder().enrollee(bundle.governedEnrollee()).build()));
        assertTrue(notSubjectOrConsentedExp.evaluate(EnrolleeSearchContext.builder().enrollee(bundle.proxy()).build()));
    }

    @Test
    @Transactional
    public void testTrimFunction(TestInfo info) {
        Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info));

        EnrolleeSearchExpression trimExp = enrolleeSearchExpressionParser.parseRule(
                "trim('  hello  ') = 'hello'"
        );

        assertTrue(trimExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrollee).build()));
    }

    @Test
    @Transactional
    public void testNestedFunction(TestInfo info) {
        Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info));

        EnrolleeSearchExpression trimExp = enrolleeSearchExpressionParser.parseRule(
                "trim(lower('  HEY  ')) = 'hey'"
        );

        assertTrue(trimExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrollee).build()));
    }

    @Test
    @Transactional
    public void testMinFunction(TestInfo info) {
        Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info));

        EnrolleeSearchExpression minExp = enrolleeSearchExpressionParser.parseRule(
                "min(20, 6, 5, 10, 8, 100) = 5"
        );

        assertTrue(minExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrollee).build()));
    }

    @Test
    @Transactional
    public void testMaxFunction(TestInfo info) {
        Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info));

        EnrolleeSearchExpression maxExp = enrolleeSearchExpressionParser.parseRule(
                "max(20, 6, 5, 10, 8, 100) = 100"
        );

        assertTrue(maxExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrollee).build()));
    }

    @Test
    @Transactional
    public void testFamilyTerm(TestInfo info) {
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(getTestName(info));
        Enrollee enrolleeNoFamily = enrolleeFactory.buildPersisted(getTestName(info), studyEnv);
        Enrollee enrolleeWithOtherFamily = enrolleeFactory.buildPersisted(getTestName(info), studyEnv);
        Enrollee enrolleeWithFamily = enrolleeFactory.buildPersisted(getTestName(info), studyEnv);

        Family family = familyFactory.buildPersisted(getTestName(info), enrolleeWithFamily);
        // create some other families to ensure no match
        familyFactory.buildPersisted(getTestName(info), enrolleeWithFamily);
        familyFactory.buildPersisted(getTestName(info), enrolleeWithOtherFamily);

        EnrolleeSearchExpression familyExp = enrolleeSearchExpressionParser.parseRule(
                "{family.shortcode} = '%s'".formatted(family.getShortcode())
        );

        assertTrue(familyExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrolleeWithFamily).build()));
        assertFalse(familyExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrolleeNoFamily).build()));
        assertFalse(familyExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrolleeWithOtherFamily).build()));
    }

    @Test
    @Transactional
    public void testAnswerCrossStudyEvaluate(TestInfo info) {
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        PortalEnvironment portalEnv = studyEnvBundle.getPortalEnv();
        StudyEnvironment studyEnv = studyEnvBundle.getStudyEnv();

        StudyEnvironmentBundle studyEnvBundle2 = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox, studyEnvBundle.getPortal(), studyEnvBundle.getPortalEnv());
        StudyEnvironment studyEnv2 = studyEnvBundle2.getStudyEnv();

        Survey surveyInEnv1 = surveyFactory.buildPersisted(getTestName(info), portalEnv.getPortalId());

        surveyFactory.attachToEnv(surveyInEnv1, studyEnv.getId(), true);

        Survey surveyInEnv2 = surveyFactory.buildPersisted(getTestName(info), portalEnv.getPortalId());

        surveyFactory.attachToEnv(surveyInEnv2, studyEnv2.getId(), true);

        EnrolleeBundle bundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv);

        Enrollee enrollee1 = bundle.enrollee();
        Enrollee enrollee2 = enrolleeFactory.buildPersisted(getTestName(info), studyEnv2.getId(), enrollee1.getParticipantUserId(), enrollee1.getProfileId());
        Enrollee unrelatedEnrollee = enrolleeFactory.buildPersisted(getTestName(info), studyEnv2);

        surveyResponseFactory.buildWithAnswers(enrollee1, surveyInEnv1, Map.of("question_env_1", "answer1"));
        surveyResponseFactory.buildWithAnswers(enrollee2, surveyInEnv2, Map.of("question_env_2", "differentAnswer"));
        surveyResponseFactory.buildWithAnswers(unrelatedEnrollee, surveyInEnv2, Map.of("question_env_2", "wrongEnrollee"));


        String study2StableId = studyEnvBundle2.getStudy().getName();
        String survey1StableId = surveyInEnv1.getStableId();
        String survey2StableId = surveyInEnv2.getStableId();

        EnrolleeSearchExpression crossStudyAnswer = enrolleeSearchExpressionParser.parseRule(
                "{answer." + survey1StableId + ".question_env_1} = 'answer1' and {answer[\"" + study2StableId + "\"]." + survey2StableId + ".question_env_2} = 'differentAnswer'"
        );

        EnrolleeSearchExpression wrongOtherStudyAnswer = enrolleeSearchExpressionParser.parseRule(
                "{answer." + survey1StableId + ".question_env_1} = 'answer1' and {answer[\"" + study2StableId + "\"]." + survey2StableId + ".question_env_2} = 'wrongEnrollee'"
        );

        EnrolleeSearchExpression wrongOtherStudyName = enrolleeSearchExpressionParser.parseRule(
                "{answer." + survey1StableId + ".question_env_1} = 'answer1' and {answer[\"notastudy\"]." + survey2StableId + ".question_env_2} = 'differentAnswer'"
        );

        EnrolleeSearchExpression wrongQuestion = enrolleeSearchExpressionParser.parseRule(
                "{answer." + survey1StableId + ".question_env_1} = 'answer1' and {answer[\"" + study2StableId + "\"]." + survey2StableId + ".wrong_question} = 'differentAnswer'"
        );


        EnrolleeSearchContext enrollee1Context = EnrolleeSearchContext.builder().enrollee(enrollee1).build();
        EnrolleeSearchContext enrollee2Context = EnrolleeSearchContext.builder().enrollee(enrollee2).build();


        assertTrue(crossStudyAnswer.evaluate(enrollee1Context));
        assertFalse(crossStudyAnswer.evaluate(enrollee2Context));

        assertFalse(wrongOtherStudyAnswer.evaluate(enrollee1Context));
        assertFalse(wrongOtherStudyAnswer.evaluate(enrollee2Context));

        assertFalse(wrongOtherStudyName.evaluate(enrollee1Context));
        assertFalse(wrongOtherStudyName.evaluate(enrollee2Context));

        assertFalse(wrongQuestion.evaluate(enrollee1Context));
        assertFalse(wrongQuestion.evaluate(enrollee2Context));
    }

    @Test
    @Transactional
    public void testNotEqualsNullable(TestInfo info) {
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(getTestName(info));
        Survey survey = surveyFactory.buildPersisted(getTestName(info));
        surveyFactory.attachToEnv(survey, studyEnv.getId(), true);

        Enrollee enrollee1 = enrolleeFactory.buildPersisted(getTestName(info), studyEnv); // has survey response and answer
        Enrollee enrollee2 = enrolleeFactory.buildPersisted(getTestName(info), studyEnv); // has survey response and answer, but not 'answer1'
        Enrollee enrollee3 = enrolleeFactory.buildPersisted(getTestName(info), studyEnv); // has survey response but no answer
        Enrollee enrollee4 = enrolleeFactory.buildPersisted(getTestName(info), studyEnv); // no survey response

        surveyResponseFactory.buildWithAnswers(enrollee1, survey, Map.of(
                "testquestion", "answer1"
        ));

        surveyResponseFactory.buildWithAnswers(enrollee2, survey, Map.of(
                "testquestion", "answer2"
        ));

        surveyResponseFactory.buildWithAnswers(enrollee3, survey, Map.of());


        EnrolleeSearchExpression searchExp = enrolleeSearchExpressionParser.parseRule(
                "{answer.%s.testquestion} != 'answer1'".formatted(survey.getStableId())
        );

        assertFalse(searchExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrollee1).build()));
        assertTrue(searchExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrollee2).build()));
        assertTrue(searchExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrollee3).build()));
        assertTrue(searchExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrollee4).build()));
    }

    @Test
    @Transactional
    public void testIsNull(TestInfo info) {
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        EnrolleeBundle enrolleeBundle1 = enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());
        EnrolleeBundle enrolleeBundle2 = enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());
        EnrolleeBundle enrolleeBundle3 = enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());
        EnrolleeBundle enrolleeBundle4 = enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());

        // enrolleeBundle1/enrolleeBundle2 have no last login, enrolleeBundle3/enrolleeBundle4 have last logins
        ParticipantUser user1 = enrolleeBundle1.participantUser();
        ParticipantUser user2 = enrolleeBundle2.participantUser();
        ParticipantUser user3 = enrolleeBundle3.participantUser();
        ParticipantUser user4 = enrolleeBundle4.participantUser();


        user1.setLastLogin(null);
        participantUserService.update(user1);

        user2.setLastLogin(null);
        participantUserService.update(user2);

        user3.setLastLogin(Instant.now());
        participantUserService.update(user3);

        user4.setLastLogin(Instant.now());
        participantUserService.update(user4);


        EnrolleeSearchExpression isNullExp = enrolleeSearchExpressionParser.parseRule(
                "{user.lastLogin} isNull"
        );

        EnrolleeSearchExpression isNotNullExp = enrolleeSearchExpressionParser.parseRule(
                "{user.lastLogin} isNotNull"
        );

        assertTrue(isNullExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrolleeBundle1.enrollee()).build()));
        assertTrue(isNullExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrolleeBundle2.enrollee()).build()));
        assertFalse(isNullExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrolleeBundle3.enrollee()).build()));
        assertFalse(isNullExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrolleeBundle4.enrollee()).build()));

        assertFalse(isNotNullExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrolleeBundle1.enrollee()).build()));
        assertFalse(isNotNullExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrolleeBundle2.enrollee()).build()));
        assertTrue(isNotNullExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrolleeBundle3.enrollee()).build()));
        assertTrue(isNotNullExp.evaluate(EnrolleeSearchContext.builder().enrollee(enrolleeBundle4.enrollee()).build()));
    }

}
