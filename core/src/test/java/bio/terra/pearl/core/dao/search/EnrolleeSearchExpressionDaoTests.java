package bio.terra.pearl.core.dao.search;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.dataimport.TimeShiftDao;
import bio.terra.pearl.core.factory.StudyEnvironmentBundle;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.kit.KitRequestFactory;
import bio.terra.pearl.core.factory.fileupload.DownloadRecordFactory;
import bio.terra.pearl.core.factory.fileupload.ParticipantFileFactory;
import bio.terra.pearl.core.factory.participant.*;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.factory.survey.SurveyResponseFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.file.ParticipantFile;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Family;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.search.EnrolleeSearchExpressionResult;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.AnswerFormat;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.kit.pepper.PepperKitStatus;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.participant.ProfileService;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpression;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import bio.terra.pearl.core.service.search.EnrolleeSearchOptions;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnrolleeSearchExpressionDaoTests extends BaseSpringBootTest {
    @Autowired
    EnrolleeSearchExpressionDao enrolleeSearchExpressionDao;
    @Autowired
    EnrolleeFactory enrolleeFactory;
    @Autowired
    StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    EnrolleeSearchExpressionParser enrolleeSearchExpressionParser;
    @Autowired
    SurveyFactory surveyFactory;
    @Autowired
    SurveyResponseFactory surveyResponseFactory;
    @Autowired
    ParticipantTaskFactory participantTaskFactory;
    @Autowired
    ParticipantTaskService participantTaskService;
    @Autowired
    KitRequestFactory kitRequestFactory;
    @Autowired
    FamilyFactory familyFactory;
    @Autowired
    PortalParticipantUserService portalParticipantUserService;
    @Autowired
    ProfileService profileService;
    @Autowired
    TimeShiftDao timeShiftDao;
    @Autowired
    ParticipantUserService participantUserService;
    @Autowired
    ParticipantFileFactory participantFileFactory;
    @Autowired
    DownloadRecordFactory downloadRecordFactory;
    @Test
    @Transactional
    public void testExecuteAnswerSearch(TestInfo info) {
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(getTestName(info));
        StudyEnvironment otherEnv = studyEnvironmentFactory.buildPersisted(getTestName(info));


        Survey survey = surveyFactory.buildPersisted(getTestName(info));

        EnrolleeSearchExpression exp = enrolleeSearchExpressionParser.parseRule(
                "{profile.givenName} = 'Jonas' and {profile.familyName} = 'Salk' and {answer.%s.test_question} = 'answer'".formatted(survey.getStableId())
        );
        surveyFactory.attachToEnv(survey, studyEnv.getId(), true);

        Assertions.assertEquals(0, enrolleeSearchExpressionDao.executeSearch(exp, studyEnv.getId()).size());

        // correct enrollee
        Enrollee salk = enrolleeFactory.buildPersisted(
                getTestName(info),
                studyEnv,
                Profile.builder().givenName("Jonas").familyName("Salk").build());
        // no answer but correct name
        enrolleeFactory.buildPersisted(
                getTestName(info),
                studyEnv,
                Profile.builder().givenName("Jonas").familyName("Salk").build());
        // wrong last name but correct answer
        Enrollee smith = enrolleeFactory.buildPersisted(
                getTestName(info),
                studyEnv,
                Profile.builder().givenName("Jonas").familyName("Smith").build());
        // wrong study env
        Enrollee wrongEnv = enrolleeFactory.buildPersisted(
                getTestName(info),
                otherEnv,
                Profile.builder().givenName("Jonas").familyName("Salk").build());


        surveyResponseFactory.buildWithAnswers(
                salk,
                survey,
                Map.of("test_question", "answer")
        );

        surveyResponseFactory.buildWithAnswers(
                smith,
                survey,
                Map.of("test_question", "answer")
        );

        surveyResponseFactory.buildWithAnswers(
                wrongEnv,
                survey,
                Map.of("test_question", "answer")
        );

        List<EnrolleeSearchExpressionResult> results = enrolleeSearchExpressionDao.executeSearch(exp, studyEnv.getId());

        Assertions.assertEquals(1, results.size());
        EnrolleeSearchExpressionResult result = results.get(0);
        Assertions.assertEquals(salk.getId(), result.getEnrollee().getId());
        Assertions.assertEquals(
                "answer",
                result.getAnswers().get(0).getStringValue()
        );
    }

    @Test
    @Transactional
    public void testAnswerAttachedToResult(TestInfo info) {
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(getTestName(info));
        Survey survey = surveyFactory.buildPersisted(getTestName(info));
        Survey diffSurvey = surveyFactory.buildPersisted(getTestName(info));
        surveyFactory.attachToEnv(survey, studyEnv.getId(), true);
        surveyFactory.attachToEnv(diffSurvey, studyEnv.getId(), true);

        String response = RandomStringUtils.randomAlphanumeric(20);

        EnrolleeSearchExpression exp = enrolleeSearchExpressionParser.parseRule(
                "{answer.%s.example_question} = '%s'".formatted(survey.getStableId(), response)
        );

        Enrollee enrollee = enrolleeFactory.buildPersisted(
                getTestName(info),
                studyEnv);

        surveyResponseFactory.buildWithAnswers(
                enrollee,
                survey,
                Map.of(
                        "example_question", response,
                        "another_question", "something else")
        );

        surveyResponseFactory.buildWithAnswers(
                enrollee,
                diffSurvey,
                Map.of(
                        "example_question", "NOT THE CORRECT ONE",
                        "another_question", "something else")
        );

        List<EnrolleeSearchExpressionResult> results = enrolleeSearchExpressionDao.executeSearch(exp, studyEnv.getId());

        Assertions.assertEquals(1, results.size());

        EnrolleeSearchExpressionResult result = results.get(0);

        Assertions.assertEquals(enrollee.getId(), result.getEnrollee().getId());

        Assertions.assertEquals(response, result.getAnswers().get(0).getStringValue());
        Assertions.assertEquals(survey.getStableId(), result.getAnswers().get(0).getSurveyStableId());
    }

    @Test
    @Transactional
    public void testMultipleAnswerSearch(TestInfo info) {
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(getTestName(info));
        Survey survey1 = surveyFactory.buildPersisted(getTestName(info));
        Survey survey2 = surveyFactory.buildPersisted(getTestName(info));
        surveyFactory.attachToEnv(survey1, studyEnv.getId(), true);
        surveyFactory.attachToEnv(survey2, studyEnv.getId(), true);


        EnrolleeSearchExpression exp = enrolleeSearchExpressionParser.parseRule(
                "{answer.%s.example_question} = 'asdf' and {answer.%s.other_question} = 'hjkl'".formatted(
                        survey1.getStableId(),
                        survey2.getStableId())
        );

        Enrollee enrollee = enrolleeFactory.buildPersisted(
                getTestName(info),
                studyEnv);

        Enrollee onlyMatchesOne = enrolleeFactory.buildPersisted(
                getTestName(info),
                studyEnv);

        surveyResponseFactory.buildWithAnswers(
                enrollee,
                survey1,
                Map.of("example_question", "asdf")
        );

        surveyResponseFactory.buildWithAnswers(
                enrollee,
                survey2,
                Map.of("other_question", "hjkl")
        );

        surveyResponseFactory.buildWithAnswers(
                onlyMatchesOne,
                survey1,
                Map.of("example_question", "asdf")
        );

        List<EnrolleeSearchExpressionResult> results = enrolleeSearchExpressionDao.executeSearch(exp, studyEnv.getId());

        Assertions.assertEquals(1, results.size());
        EnrolleeSearchExpressionResult result = results.get(0);
        Assertions.assertEquals(enrollee.getId(), result.getEnrollee().getId());

        Assertions.assertEquals(2, result.getAnswers().size());
        Assertions.assertTrue(result.getAnswers().stream().anyMatch(
                a -> a.getSurveyStableId().equals(survey1.getStableId()) && a.getQuestionStableId().equals("example_question")));
        Assertions.assertTrue(result.getAnswers().stream().anyMatch(
                a -> a.getSurveyStableId().equals(survey2.getStableId()) && a.getQuestionStableId().equals("other_question")));
    }

    @Test
    @Transactional
    public void testAgeFacet(TestInfo info) {
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(getTestName(info));
        EnrolleeSearchExpression exp = enrolleeSearchExpressionParser.parseRule(
                "{age} > 25 and {age} <= 30"
        );

        enrolleeFactory.buildPersisted(
                getTestName(info),
                studyEnv,
                Profile.builder().birthDate(LocalDate.now().minusYears(32)).build());
        Enrollee e1 = enrolleeFactory.buildPersisted(
                getTestName(info),
                studyEnv,
                Profile.builder().birthDate(LocalDate.now().minusYears(27)).build());
        Enrollee e2 = enrolleeFactory.buildPersisted(
                getTestName(info),
                studyEnv,
                Profile.builder().birthDate(LocalDate.now().minusYears(26)).build());
        Enrollee e3 = enrolleeFactory.buildPersisted(
                getTestName(info),
                studyEnv,
                Profile.builder().birthDate(LocalDate.now().minusYears(30)).build());
        enrolleeFactory.buildPersisted(
                getTestName(info),
                studyEnv,
                Profile.builder().birthDate(LocalDate.now().minusYears(25)).build());


        List<EnrolleeSearchExpressionResult> results = enrolleeSearchExpressionDao.executeSearch(exp, studyEnv.getId());

        Assertions.assertEquals(3, results.size());

        Assertions.assertTrue(results.stream().anyMatch(r -> r.getEnrollee().getId().equals(e1.getId())));
        Assertions.assertTrue(results.stream().anyMatch(r -> r.getEnrollee().getId().equals(e2.getId())));
        Assertions.assertTrue(results.stream().anyMatch(r -> r.getEnrollee().getId().equals(e3.getId())));
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

        // enrollee with no response
        enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);


        String rule = "{answer.%s.country} = 'us' and ({answer.%s.diagnosis} = 'diag1' or {answer.%s.diagnosis} = 'diag2')".formatted(survey.getStableId(), survey.getStableId(), survey.getStableId());
        EnrolleeSearchExpression searchExp = enrolleeSearchExpressionParser.parseRule(rule);


        List<EnrolleeSearchExpressionResult> results = enrolleeSearchExpressionDao.executeSearch(searchExp, studyEnvironment.getId());

        Assertions.assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(r -> r.getEnrollee().getId().equals(enrolleeMatches1.getId())));
        assertTrue(results.stream().anyMatch(r -> r.getEnrollee().getId().equals(enrolleeMatches2.getId())));
    }


    @Test
    @Transactional
    public void testTaskFacets(TestInfo info) {
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        EnrolleeSearchExpression assignedExp = enrolleeSearchExpressionParser.parseRule(
                "{task.demographic_survey.assigned} = true"
        );

        EnrolleeSearchExpression inProgressExp = enrolleeSearchExpressionParser.parseRule(
                "{task.demographic_survey.status} = 'IN_PROGRESS'"
        );
        EnrolleeSearchExpression completed3DaysAgoExp = enrolleeSearchExpressionParser.parseRule(
                "{task.completed_task.completedDaysAgo} > 3"
        );
        EnrolleeSearchExpression completed5DaysAgoExp = enrolleeSearchExpressionParser.parseRule(
                "{task.completed_task.completedDaysAgo} > 5"
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

        // completed task 4 days ago
        EnrolleeBundle eBundleCompletedTask = enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());
        ParticipantTask completedTask = participantTaskFactory.buildPersisted(eBundleCompletedTask, "completed_task", TaskStatus.COMPLETE, TaskType.SURVEY);
        completedTask.setCompletedAt(Instant.now().minus(4, ChronoUnit.DAYS));
        participantTaskService.update(completedTask, DataAuditInfo.builder().systemProcess(getTestName(info)).build());
        Enrollee enrolleeCompletedTask = eBundleCompletedTask.enrollee();

        List<EnrolleeSearchExpressionResult> resultsAssigned = enrolleeSearchExpressionDao.executeSearch(assignedExp, studyEnvBundle.getStudyEnv().getId());
        List<EnrolleeSearchExpressionResult> resultsInProgress = enrolleeSearchExpressionDao.executeSearch(inProgressExp, studyEnvBundle.getStudyEnv().getId());

        Assertions.assertEquals(2, resultsAssigned.size());
        Assertions.assertEquals(1, resultsInProgress.size());

        assertTrue(resultsAssigned.stream().anyMatch(r -> r.getEnrollee().getId().equals(enrolleeNotStarted.getId())));
        assertTrue(resultsAssigned.stream().anyMatch(r -> r.getEnrollee().getId().equals(enrolleeInProgress.getId())));

        assertTrue(resultsInProgress.stream().anyMatch(r -> r.getEnrollee().getId().equals(enrolleeInProgress.getId())));

        // attaches the task to the enrollee search result
        assertTrue(resultsInProgress.stream().allMatch(r -> r.getTasks().size() == 1 && r.getTasks().get(0).getTargetStableId().equals("demographic_survey")));
        assertTrue(resultsAssigned.stream().allMatch(r -> r.getTasks().size() == 1 && r.getTasks().get(0).getTargetStableId().equals("demographic_survey")));

        // check days ago expressions
        List<EnrolleeSearchExpressionResult> completed3daysAgo = enrolleeSearchExpressionDao.executeSearch(completed3DaysAgoExp, studyEnvBundle.getStudyEnv().getId());
        Assertions.assertEquals(1, completed3daysAgo.size());
        assertTrue(completed3daysAgo.stream().anyMatch(r -> r.getEnrollee().getId().equals(enrolleeCompletedTask.getId())));

        List<EnrolleeSearchExpressionResult> completed5daysAgo = enrolleeSearchExpressionDao.executeSearch(completed5DaysAgoExp, studyEnvBundle.getStudyEnv().getId());
        Assertions.assertEquals(0, completed5daysAgo.size());
    }

    /** include({task.*}) should add the column without filtering enrollees who don't have the task */
    @Test
    @Transactional
    public void testIncludeTaskFacetDoesNotFilter(TestInfo info) {
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);

        EnrolleeBundle withTask = enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());
        participantTaskFactory.buildPersisted(withTask, "demographic_survey", TaskStatus.NEW, TaskType.SURVEY);

        EnrolleeBundle withoutTask = enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());

        EnrolleeSearchExpression includeExp = enrolleeSearchExpressionParser.parseRule(
                "include({task.demographic_survey.assigned})"
        );

        List<EnrolleeSearchExpressionResult> results = enrolleeSearchExpressionDao.executeSearch(includeExp, studyEnvBundle.getStudyEnv().getId());

        // both enrollees should be returned — include() must not filter
        Assertions.assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(r -> r.getEnrollee().getId().equals(withTask.enrollee().getId())));
        assertTrue(results.stream().anyMatch(r -> r.getEnrollee().getId().equals(withoutTask.enrollee().getId())));
    }

    @Test
    @Transactional
    public void testEnrolleeFacets(TestInfo info) {
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        UUID studyEnvId = studyEnvBundle.getStudyEnv().getId();
        EnrolleeSearchExpression consentedExp = enrolleeSearchExpressionParser.parseRule(
                "{enrollee.consented} = true"
        );

        EnrolleeSearchExpression subjectExp = enrolleeSearchExpressionParser.parseRule(
                "{enrollee.subject} = true"
        );

        EnrolleeSearchExpression shortcodeExp = enrolleeSearchExpressionParser.parseRule(
                "{enrollee.shortcode} = 'EXAMPLE'"
        );

        EnrolleeSearchExpression researchIdExp = enrolleeSearchExpressionParser.parseRule(
                "{enrollee.researchId} = '12EXAMPLE'"
        );

        Enrollee notConsented = enrolleeFactory.buildPersisted(
                enrolleeFactory.builderWithDependencies(getTestName(info)).consented(false).subject(true).studyEnvironmentId(studyEnvId));
        timeShiftDao.changeEnrolleeCreationTime(notConsented.getId(), Instant.now().minus(5, ChronoUnit.DAYS));
        Enrollee notSubject = enrolleeFactory.buildPersisted(
                enrolleeFactory.builderWithDependencies(getTestName(info)).consented(false).subject(false).studyEnvironmentId(studyEnvId));
        timeShiftDao.changeEnrolleeCreationTime(notSubject.getId(), Instant.now().minus(4, ChronoUnit.DAYS));
        Enrollee specialShortcode = enrolleeFactory.buildPersisted(
                enrolleeFactory.builderWithDependencies(getTestName(info)).consented(false).subject(false).shortcode("EXAMPLE").studyEnvironmentId(studyEnvId));
        timeShiftDao.changeEnrolleeCreationTime(specialShortcode.getId(), Instant.now().minus(2, ChronoUnit.DAYS));
        Enrollee consented = enrolleeFactory.buildPersisted(
                enrolleeFactory.builderWithDependencies(getTestName(info)).consented(true).subject(true).studyEnvironmentId(studyEnvId));
        timeShiftDao.changeEnrolleeCreationTime(consented.getId(), Instant.now().minus(1, ChronoUnit.DAYS));
        Enrollee specialResearchId = enrolleeFactory.buildPersisted(
                enrolleeFactory.builderWithDependencies(getTestName(info)).consented(false).subject(false).researchId("12EXAMPLE").studyEnvironmentId(studyEnvId));
        timeShiftDao.changeEnrolleeCreationTime(specialShortcode.getId(), Instant.now().minus(0, ChronoUnit.DAYS));


        List<EnrolleeSearchExpressionResult> resultsConsented = enrolleeSearchExpressionDao.executeSearch(consentedExp, studyEnvBundle.getStudyEnv().getId());
        List<EnrolleeSearchExpressionResult> resultsSubject = enrolleeSearchExpressionDao.executeSearch(subjectExp, studyEnvBundle.getStudyEnv().getId());
        List<EnrolleeSearchExpressionResult> resultsShortcode = enrolleeSearchExpressionDao.executeSearch(shortcodeExp, studyEnvBundle.getStudyEnv().getId());
        List<EnrolleeSearchExpressionResult> resultsResearchId = enrolleeSearchExpressionDao.executeSearch(researchIdExp, studyEnvBundle.getStudyEnv().getId());

        Assertions.assertEquals(1, resultsConsented.size());
        Assertions.assertEquals(2, resultsSubject.size());
        Assertions.assertEquals(1, resultsShortcode.size());
        Assertions.assertEquals(1, resultsResearchId.size());

        assertTrue(resultsConsented.stream().anyMatch(r -> r.getEnrollee().getId().equals(consented.getId())));
        assertTrue(resultsSubject.stream().anyMatch(r -> r.getEnrollee().getId().equals(notConsented.getId())));
        assertTrue(resultsSubject.stream().anyMatch(r -> r.getEnrollee().getId().equals(consented.getId())));
        assertTrue(resultsShortcode.stream().anyMatch(r -> r.getEnrollee().getId().equals(specialShortcode.getId())));
        assertTrue(resultsResearchId.stream().anyMatch(r -> r.getEnrollee().getId().equals(specialResearchId.getId())));

        // now check time filtering
        List<EnrolleeSearchExpressionResult> resultsTime = enrolleeSearchExpressionDao.executeSearch(
                enrolleeSearchExpressionParser.parseRule("{enrollee.createdAt} < '%s'".formatted(Instant.now().minus(3, ChronoUnit.DAYS))),
                studyEnvBundle.getStudyEnv().getId()
        );
        assertThat(resultsTime.stream().map(EnrolleeSearchExpressionResult::getEnrollee).map(Enrollee::getId).toList(),
                containsInAnyOrder(notConsented.getId(), notSubject.getId()));

        resultsTime = enrolleeSearchExpressionDao.executeSearch(
                enrolleeSearchExpressionParser.parseRule("{enrollee.createdAt} > '%s'".formatted(Instant.now().minus(3, ChronoUnit.DAYS))),
                studyEnvBundle.getStudyEnv().getId()
        );
        assertThat(resultsTime.stream().map(EnrolleeSearchExpressionResult::getEnrollee).map(Enrollee::getId).toList(),
                containsInAnyOrder(specialShortcode.getId(), consented.getId(), specialResearchId.getId()));

    }

    @Test
    @Transactional
    public void testContains(TestInfo info) {
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        UUID studyEnvId = studyEnvBundle.getStudyEnv().getId();

        EnrolleeSearchExpression shortcodeExp = enrolleeSearchExpressionParser.parseRule(
                "{enrollee.shortcode} contains 'JSA'"
        );

        Enrollee startsWith = enrolleeFactory.buildPersisted(
                enrolleeFactory.builderWithDependencies(getTestName(info)).shortcode("JSALK").studyEnvironmentId(studyEnvId));

        Enrollee within = enrolleeFactory.buildPersisted(
                enrolleeFactory.builderWithDependencies(getTestName(info)).shortcode("ASDJSAF").studyEnvironmentId(studyEnvId));

        Enrollee endsWith = enrolleeFactory.buildPersisted(
                enrolleeFactory.builderWithDependencies(getTestName(info)).shortcode("ASDFJSA").studyEnvironmentId(studyEnvId));

        Enrollee doesNotContain = enrolleeFactory.buildPersisted(
                enrolleeFactory.builderWithDependencies(getTestName(info)).shortcode("PSALK").studyEnvironmentId(studyEnvId));

        List<EnrolleeSearchExpressionResult> resultsShortcode = enrolleeSearchExpressionDao.executeSearch(shortcodeExp, studyEnvBundle.getStudyEnv().getId());

        Assertions.assertEquals(3, resultsShortcode.size());
        assertTrue(resultsShortcode.stream().anyMatch(r -> r.getEnrollee().getId().equals(startsWith.getId())));
        assertTrue(resultsShortcode.stream().anyMatch(r -> r.getEnrollee().getId().equals(within.getId())));
        assertTrue(resultsShortcode.stream().anyMatch(r -> r.getEnrollee().getId().equals(endsWith.getId())));
    }

    @Test
    @Transactional
    public void testProfileName(TestInfo info) {
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        PortalEnvironment portalEnv = studyEnvBundle.getPortalEnv();
        StudyEnvironment studyEnv = studyEnvBundle.getStudyEnv();

        EnrolleeSearchExpression nameExp = enrolleeSearchExpressionParser.parseRule(
                "{profile.name} = 'Jonas Salk'"
        );


        EnrolleeBundle jsalkBundle = enrolleeFactory.buildWithPortalUser(
                getTestName(info),
                portalEnv,
                studyEnv,
                Profile.builder().givenName("Jonas").familyName("Salk").build());

        EnrolleeBundle psalkBundle = enrolleeFactory.buildWithPortalUser(
                getTestName(info),
                portalEnv,
                studyEnv,
                Profile.builder().givenName("Peter").familyName("Salk").build());

        EnrolleeBundle reversedBundle = enrolleeFactory.buildWithPortalUser(
                getTestName(info),
                portalEnv,
                studyEnv,
                Profile.builder().givenName("Salk").familyName("Jonas").build());


        List<EnrolleeSearchExpressionResult> resultsName = enrolleeSearchExpressionDao.executeSearch(nameExp, studyEnv.getId());

        Assertions.assertEquals(1, resultsName.size());
        assertTrue(resultsName.stream().anyMatch(r -> r.getEnrollee().getId().equals(jsalkBundle.enrollee().getId())));
    }

    @Test
    @Transactional
    public void testLatestKit(TestInfo info) throws JsonProcessingException {
        Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info));
        kitRequestFactory.buildPersisted(getTestName(info), enrollee, PepperKitStatus.CREATED);

        EnrolleeSearchExpression createdExp = enrolleeSearchExpressionParser.parseRule(
                "{latestKit.status} = 'CREATED'"
        );

        EnrolleeSearchExpression erroredExp = enrolleeSearchExpressionParser.parseRule(
                "{latestKit.status} = 'ERRORED'"
        );

        List<EnrolleeSearchExpressionResult> resultsCreated =
                enrolleeSearchExpressionDao.executeSearch(
                        createdExp,
                        enrollee.getStudyEnvironmentId());

        assertEquals(1, resultsCreated.size());
        assertEquals(enrollee.getId(), resultsCreated.get(0).getEnrollee().getId());
        assertEquals(KitRequestStatus.CREATED, resultsCreated.get(0).getLatestKit().getStatus());

        List<EnrolleeSearchExpressionResult> resultsErrored =
                enrolleeSearchExpressionDao.executeSearch(
                        erroredExp,
                        enrollee.getStudyEnvironmentId());

        assertEquals(0, resultsErrored.size());

        kitRequestFactory.buildPersisted(getTestName(info), enrollee, PepperKitStatus.ERRORED);

        resultsErrored =
                enrolleeSearchExpressionDao.executeSearch(
                        erroredExp,
                        enrollee.getStudyEnvironmentId());

        assertEquals(1, resultsErrored.size());
        assertEquals(enrollee.getId(), resultsErrored.get(0).getEnrollee().getId());
        assertEquals(KitRequestStatus.ERRORED, resultsErrored.get(0).getLatestKit().getStatus());

        resultsCreated =
                enrolleeSearchExpressionDao.executeSearch(
                        createdExp,
                        enrollee.getStudyEnvironmentId());

        assertEquals(0, resultsCreated.size());
    }

    @Test
    @Transactional
    public void testPortalUser(TestInfo info) {
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        EnrolleeBundle bundle = enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());
        Instant loginTime = Instant.now();
        bundle.portalParticipantUser().setLastLogin(loginTime);
        portalParticipantUserService.update(bundle.portalParticipantUser());

        EnrolleeBundle bundle2 = enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());
        loginTime = Instant.now().minusSeconds(3600);
        bundle2.portalParticipantUser().setLastLogin(loginTime);
        portalParticipantUserService.update(bundle2.portalParticipantUser());

        EnrolleeSearchExpression nameExp = enrolleeSearchExpressionParser.parseRule("{portalUser.lastLogin} < {user.createdAt}");

        List<EnrolleeSearchExpressionResult> resultsName = enrolleeSearchExpressionDao.executeSearch(nameExp, studyEnvBundle.getStudyEnv().getId());

        Assertions.assertEquals(1, resultsName.size());
        assertTrue(resultsName.stream().anyMatch(r -> r.getEnrollee().getId().equals(bundle2.enrollee().getId())));
    }

    @Test
    @Transactional
    public void testLowerFunction(TestInfo info) {
        Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info));

        EnrolleeSearchExpression lowerExp = enrolleeSearchExpressionParser.parseRule(
                "lower({enrollee.shortcode}) = '" + enrollee.getShortcode().toLowerCase() + "'"
        );

        List<EnrolleeSearchExpressionResult> results = enrolleeSearchExpressionDao.executeSearch(lowerExp, enrollee.getStudyEnvironmentId());

        assertEquals(1, results.size());
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

        List<EnrolleeSearchExpressionResult> resultsNotSubject = enrolleeSearchExpressionDao.executeSearch(notSubjectExp, bundle.governedEnrollee().getStudyEnvironmentId());
        List<EnrolleeSearchExpressionResult> resultsNotSubjectOrConsented = enrolleeSearchExpressionDao.executeSearch(notSubjectOrConsentedExp, bundle.governedEnrollee().getStudyEnvironmentId());

        assertEquals(1, resultsNotSubject.size());
        assertEquals(1, resultsNotSubjectOrConsented.size());

        assertTrue(resultsNotSubject.stream().anyMatch(r -> r.getEnrollee().getId().equals(bundle.proxy().getId())));
        assertTrue(resultsNotSubjectOrConsented.stream().anyMatch(r -> r.getEnrollee().getId().equals(bundle.proxy().getId())));
    }

    @Test
    @Transactional
    public void testTrimFunction(TestInfo info) {
        Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info));

        EnrolleeSearchExpression trimExp = enrolleeSearchExpressionParser.parseRule(
                "trim('  hello  ') = 'hello'"
        );

        List<EnrolleeSearchExpressionResult> results = enrolleeSearchExpressionDao.executeSearch(trimExp, enrollee.getStudyEnvironmentId());

        // should true for everybody
        assertEquals(1, results.size());
    }

    @Test
    @Transactional
    public void testNestedFunction(TestInfo info) {
        Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info));

        EnrolleeSearchExpression trimExp = enrolleeSearchExpressionParser.parseRule(
                "trim(lower('  HEY  ')) = 'hey'"
        );

        List<EnrolleeSearchExpressionResult> results = enrolleeSearchExpressionDao.executeSearch(trimExp, enrollee.getStudyEnvironmentId());

        // should true for everybody
        assertEquals(1, results.size());
    }

    @Test
    @Transactional
    public void testMinFunction(TestInfo info) {
        Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info));

        EnrolleeSearchExpression minExp = enrolleeSearchExpressionParser.parseRule(
                "min(20, 6, 5, 10, 8, 100) = 5"
        );

        List<EnrolleeSearchExpressionResult> results = enrolleeSearchExpressionDao.executeSearch(minExp, enrollee.getStudyEnvironmentId());

        // should true for everybody
        assertEquals(1, results.size());
    }

    @Test
    @Transactional
    public void testMaxFunction(TestInfo info) {
        Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info));

        EnrolleeSearchExpression maxExp = enrolleeSearchExpressionParser.parseRule(
                "max(20, 6, 5, 10, 8, 100) = 100"
        );

        List<EnrolleeSearchExpressionResult> results = enrolleeSearchExpressionDao.executeSearch(maxExp, enrollee.getStudyEnvironmentId());

        // should true for everybody
        assertEquals(1, results.size());
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

        List<EnrolleeSearchExpressionResult> results = enrolleeSearchExpressionDao.executeSearch(familyExp, enrolleeWithFamily.getStudyEnvironmentId());

        assertEquals(1, results.size());
        assertEquals(enrolleeWithFamily.getId(), results.get(0).getEnrollee().getId());
        assertEquals(1, results.get(0).getFamilies().size());
        assertEquals(family.getId(), results.get(0).getFamilies().get(0).getId());
    }

    @Test
    @Transactional
    public void testIncludeFamily(TestInfo info) {
        Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info));
        Family family = familyFactory.buildPersisted(getTestName(info), enrollee);

        EnrolleeSearchExpression defaultExp = enrolleeSearchExpressionParser.parseRule(
                ""
        );

        EnrolleeSearchExpression includeExp = enrolleeSearchExpressionParser.parseRule(
                "include({family.shortcode})"
        );

        List<EnrolleeSearchExpressionResult> results = enrolleeSearchExpressionDao.executeSearch(defaultExp, enrollee.getStudyEnvironmentId());

        assertEquals(1, results.size());

        EnrolleeSearchExpressionResult result = results.get(0);
        assertEquals(enrollee.getId(), result.getEnrollee().getId());
        assertEquals(0, result.getFamilies().size());


        results = enrolleeSearchExpressionDao.executeSearch(includeExp, enrollee.getStudyEnvironmentId());

        assertEquals(1, results.size());

        result = results.get(0);

        assertEquals(enrollee.getId(), result.getEnrollee().getId());
        assertEquals(1, result.getFamilies().size());
        assertEquals(family.getId(), result.getFamilies().get(0).getId());
    }

    @Test
    @Transactional
    public void testIncludeUserData(TestInfo info) {
        EnrolleeBundle bundle = enrolleeFactory.buildWithPortalUser(getTestName(info));
        Instant loginTime = Instant.now();
        bundle.portalParticipantUser().setLastLogin(loginTime);
        portalParticipantUserService.update(bundle.portalParticipantUser());
        EnrolleeSearchExpression defaultExp = enrolleeSearchExpressionParser.parseRule("");

        List<EnrolleeSearchExpressionResult> results = enrolleeSearchExpressionDao.executeSearch(defaultExp, bundle.enrollee().getStudyEnvironmentId());
        assertEquals(1, results.size());
        EnrolleeSearchExpressionResult result = results.get(0);
        assertThat(results.get(0).getPortalParticipantUser(), nullValue());
        assertThat(results.get(0).getParticipantUser(), nullValue());

        EnrolleeSearchExpression includeExp = enrolleeSearchExpressionParser.parseRule(
                "include({user.username}) and include({portalUser.lastLogin})"
        );
        results = enrolleeSearchExpressionDao.executeSearch(includeExp, bundle.enrollee().getStudyEnvironmentId());

        assertEquals(1, results.size());
        // we can't check exact equality since it goes to the DB, so instead just confirm seconds
        assertThat(results.get(0).getPortalParticipantUser().getLastLogin().truncatedTo(ChronoUnit.MILLIS),
                equalTo(loginTime.truncatedTo(ChronoUnit.MILLIS)));
        assertThat(results.get(0).getParticipantUser().getUsername(), equalTo(bundle.participantUser().getUsername()));
    }

    @Test
    @Transactional
    public void testCrossStudyAnswerReferencing(TestInfo info) {
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

        List<EnrolleeSearchExpressionResult> results = enrolleeSearchExpressionDao.executeSearch(crossStudyAnswer, studyEnv.getId());
        assertEquals(1, results.size());
        EnrolleeSearchExpressionResult result = results.get(0);
        assertEquals(enrollee1.getId(), result.getEnrollee().getId());

        results = enrolleeSearchExpressionDao.executeSearch(wrongOtherStudyAnswer, studyEnv.getId());
        assertEquals(0, results.size());

        results = enrolleeSearchExpressionDao.executeSearch(wrongOtherStudyName, studyEnv.getId());
        assertEquals(0, results.size());

        results = enrolleeSearchExpressionDao.executeSearch(wrongQuestion, studyEnv.getId());
        assertEquals(0, results.size());
    }

    @Test
    @Transactional
    public void testIncludeTasks(TestInfo info) {
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb);
        EnrolleeBundle enrolleeBundle1 = enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());
        ParticipantTask task1 = participantTaskFactory.buildPersisted(enrolleeBundle1, "surveyA", TaskStatus.NEW, TaskType.SURVEY);
        ParticipantTask task2 = participantTaskFactory.buildPersisted(enrolleeBundle1, "surveyB", TaskStatus.NEW, TaskType.SURVEY);

        EnrolleeBundle enrolleeBundle2 = enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());
        EnrolleeBundle enrolleeBundle3 =  enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());
        ParticipantTask task3 = participantTaskFactory.buildPersisted(enrolleeBundle3, "surveyB", TaskStatus.NEW, TaskType.SURVEY);


        List<EnrolleeSearchExpressionResult> results = enrolleeSearchExpressionDao.executeSearch(
                enrolleeSearchExpressionParser.parseRule(""), studyEnvBundle.getStudyEnv().getId());
        Assertions.assertEquals(3, results.size());
        // check does not include tasks by default
        Assertions.assertTrue(results.stream().allMatch(r -> r.getTasks().isEmpty()));

        results = enrolleeSearchExpressionDao.executeSearch(
                enrolleeSearchExpressionParser.parseRule(""), studyEnvBundle.getStudyEnv().getId(),
                EnrolleeSearchOptions.builder().includes(List.of(EnrolleeSearchOptions.Include.tasks)).build());
        Assertions.assertEquals(3, results.size());
        // check the tasks are included correctly with each user
        Assertions.assertTrue(results.stream().anyMatch(r -> r.getEnrollee().getId().equals(enrolleeBundle1.enrollee().getId()) &&
                r.getTasks().size() == 2 &&
                r.getTasks().stream().allMatch(t -> t.getId().equals(task1.getId()) || t.getId().equals(task2.getId()))));
        Assertions.assertTrue(results.stream().anyMatch(r -> r.getEnrollee().getId().equals(enrolleeBundle2.enrollee().getId()) &&
                r.getTasks().size() == 0));
        Assertions.assertTrue(results.stream().anyMatch(r -> r.getEnrollee().getId().equals(enrolleeBundle3.enrollee().getId()) &&
                r.getTasks().size() == 1 &&
                r.getTasks().stream().allMatch(t -> t.getId().equals(task3.getId()))));
    }

    @Test
    @Transactional
    public void testIncludeKitRequests(TestInfo info) {
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb);
        EnrolleeBundle enrolleeBundle1 = enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());
        KitRequest kitRequest1 = kitRequestFactory.buildPersisted(getTestName(info), enrolleeBundle1.enrollee(), PepperKitStatus.CREATED);
        KitRequest kitRequest2 = kitRequestFactory.buildPersisted(getTestName(info), enrolleeBundle1.enrollee(), PepperKitStatus.SENT);

        EnrolleeBundle enrolleeBundle2 = enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());
        EnrolleeBundle enrolleeBundle3 =  enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());
        KitRequest kitRequest3 = kitRequestFactory.buildPersisted(getTestName(info), enrolleeBundle3.enrollee(), PepperKitStatus.ERRORED);

        List<EnrolleeSearchExpressionResult> results = enrolleeSearchExpressionDao.executeSearch(
                enrolleeSearchExpressionParser.parseRule(""), studyEnvBundle.getStudyEnv().getId());
        Assertions.assertEquals(3, results.size());
        // check does not include kits by default
        Assertions.assertTrue(results.stream().allMatch(r -> r.getKitRequests().isEmpty()));

        results = enrolleeSearchExpressionDao.executeSearch(
                enrolleeSearchExpressionParser.parseRule(""), studyEnvBundle.getStudyEnv().getId(),
                EnrolleeSearchOptions.builder().includes(List.of(EnrolleeSearchOptions.Include.kitRequests)).build());
        Assertions.assertEquals(3, results.size());
        // check the tasks are included correctly with each user
        Assertions.assertTrue(results.stream().anyMatch(r -> r.getEnrollee().getId().equals(enrolleeBundle1.enrollee().getId()) &&
                r.getKitRequests().size() == 2 &&
                r.getKitRequests().stream().allMatch(t -> t.getId().equals(kitRequest1.getId()) || t.getId().equals(kitRequest2.getId()))));
        Assertions.assertTrue(results.stream().anyMatch(r -> r.getEnrollee().getId().equals(enrolleeBundle2.enrollee().getId()) &&
                r.getKitRequests().size() == 0));
        Assertions.assertTrue(results.stream().anyMatch(r -> r.getEnrollee().getId().equals(enrolleeBundle3.enrollee().getId()) &&
                r.getKitRequests().size() == 1 &&
                r.getKitRequests().stream().allMatch(t -> t.getId().equals(kitRequest3.getId()))));
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

        List<EnrolleeSearchExpressionResult> results = enrolleeSearchExpressionDao.executeSearch(searchExp, studyEnv.getId());

        Assertions.assertEquals(3, results.size());
        Assertions.assertTrue(results.stream().anyMatch(r -> r.getEnrollee().getId().equals(enrollee2.getId())));
        Assertions.assertTrue(results.stream().anyMatch(r -> r.getEnrollee().getId().equals(enrollee3.getId())));
        Assertions.assertTrue(results.stream().anyMatch(r -> r.getEnrollee().getId().equals(enrollee4.getId())));
    }

    @Transactional
    @Test
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
                "{user.lastLogin} = null"
        );

        EnrolleeSearchExpression isNotNullExp = enrolleeSearchExpressionParser.parseRule(
                "{user.lastLogin} != null"
        );

        List<EnrolleeSearchExpressionResult> resultsIsNull = enrolleeSearchExpressionDao.executeSearch(isNullExp, studyEnvBundle.getStudyEnv().getId());
        List<EnrolleeSearchExpressionResult> resultsIsNotNull = enrolleeSearchExpressionDao.executeSearch(isNotNullExp, studyEnvBundle.getStudyEnv().getId());

        Assertions.assertEquals(2, resultsIsNull.size());
        Assertions.assertEquals(2, resultsIsNotNull.size());

        assertTrue(resultsIsNull.stream().anyMatch(r -> r.getEnrollee().getId().equals(enrolleeBundle1.enrollee().getId())));
        assertTrue(resultsIsNull.stream().anyMatch(r -> r.getEnrollee().getId().equals(enrolleeBundle2.enrollee().getId())));

        assertTrue(resultsIsNotNull.stream().anyMatch(r -> r.getEnrollee().getId().equals(enrolleeBundle3.enrollee().getId())));
        assertTrue(resultsIsNotNull.stream().anyMatch(r -> r.getEnrollee().getId().equals(enrolleeBundle4.enrollee().getId())));
    }

    @Test
    @Transactional
    public void testUserShortcodeSearch(TestInfo info) {
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        EnrolleeBundle bundle1 = enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());
        EnrolleeBundle bundle2 = enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());

        String targetShortcode = bundle1.participantUser().getShortcode();

        EnrolleeSearchExpression exactExp = enrolleeSearchExpressionParser.parseRule(
                "{user.shortcode} = '%s'".formatted(targetShortcode)
        );
        EnrolleeSearchExpression containsExp = enrolleeSearchExpressionParser.parseRule(
                "{user.shortcode} contains '%s'".formatted(targetShortcode)
        );

        List<EnrolleeSearchExpressionResult> exactResults = enrolleeSearchExpressionDao.executeSearch(exactExp, studyEnvBundle.getStudyEnv().getId());
        List<EnrolleeSearchExpressionResult> containsResults = enrolleeSearchExpressionDao.executeSearch(containsExp, studyEnvBundle.getStudyEnv().getId());

        Assertions.assertEquals(1, exactResults.size());
        Assertions.assertEquals(1, containsResults.size());
        assertTrue(exactResults.stream().anyMatch(r -> r.getEnrollee().getId().equals(bundle1.enrollee().getId())));
        assertTrue(containsResults.stream().anyMatch(r -> r.getEnrollee().getId().equals(bundle1.enrollee().getId())));
    }

    @Test
    @Transactional
    public void testFileUploadSearch(TestInfo info) {
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        UUID studyEnvId = studyEnvBundle.getStudyEnv().getId();

        EnrolleeSearchExpression uploadedExp = enrolleeSearchExpressionParser.parseRule("{fileUpload.my_question} = true");
        EnrolleeSearchExpression notUploadedExp = enrolleeSearchExpressionParser.parseRule("{fileUpload.my_question} = false");

        Survey survey = surveyFactory.buildPersisted(getTestName(info));

        // enrollee with a file uploaded in response to my_question
        Enrollee withFile = enrolleeFactory.buildPersisted(getTestName(info), studyEnvBundle.getStudyEnv());
        ParticipantFile file = participantFileFactory.buildPersisted(withFile);
        surveyResponseFactory.buildWithAnswers(withFile, survey, Map.of(
                "my_question", Answer.builder()
                        .format(AnswerFormat.FILE_UPLOAD)
                        .objectValue("[{\"fileName\":\"%s\"}]".formatted(file.getFileName()))
                        .build()));

        // enrollee with a file uploaded for a different question
        Enrollee wrongQuestion = enrolleeFactory.buildPersisted(getTestName(info), studyEnvBundle.getStudyEnv());
        ParticipantFile wrongFile = participantFileFactory.buildPersisted(wrongQuestion);
        surveyResponseFactory.buildWithAnswers(wrongQuestion, survey, Map.of(
                "other_question", Answer.builder()
                        .format(AnswerFormat.FILE_UPLOAD)
                        .objectValue("[{\"fileName\":\"%s\"}]".formatted(wrongFile.getFileName()))
                        .build()));

        // enrollee with no file
        Enrollee noFile = enrolleeFactory.buildPersisted(getTestName(info), studyEnvBundle.getStudyEnv());

        List<EnrolleeSearchExpressionResult> uploadedResults = enrolleeSearchExpressionDao.executeSearch(uploadedExp, studyEnvId);
        List<EnrolleeSearchExpressionResult> notUploadedResults = enrolleeSearchExpressionDao.executeSearch(notUploadedExp, studyEnvId);

        assertEquals(1, uploadedResults.size());
        assertTrue(uploadedResults.stream().anyMatch(r -> r.getEnrollee().getId().equals(withFile.getId())));

        assertEquals(2, notUploadedResults.size());
        assertTrue(notUploadedResults.stream().anyMatch(r -> r.getEnrollee().getId().equals(wrongQuestion.getId())));
        assertTrue(notUploadedResults.stream().anyMatch(r -> r.getEnrollee().getId().equals(noFile.getId())));
    }

    @Test
    @Transactional
    public void testFileDownloadSearch(TestInfo info) {
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        UUID studyEnvId = studyEnvBundle.getStudyEnv().getId();

        EnrolleeSearchExpression downloadedExp = enrolleeSearchExpressionParser.parseRule("{fileDownload.my_question} = true");
        EnrolleeSearchExpression notDownloadedExp = enrolleeSearchExpressionParser.parseRule("{fileDownload.my_question} = false");

        Survey survey = surveyFactory.buildPersisted(getTestName(info));

        // enrollee with a file that has been downloaded
        Enrollee withDownload = enrolleeFactory.buildPersisted(getTestName(info), studyEnvBundle.getStudyEnv());
        ParticipantFile downloadedFile = participantFileFactory.buildPersisted(withDownload);
        surveyResponseFactory.buildWithAnswers(withDownload, survey, Map.of(
                "my_question", Answer.builder()
                        .format(AnswerFormat.FILE_UPLOAD)
                        .objectValue("[{\"fileName\":\"%s\"}]".formatted(downloadedFile.getFileName()))
                        .build()));
        downloadRecordFactory.buildPersisted(downloadedFile);

        // enrollee with a file that has not been downloaded
        Enrollee noDownload = enrolleeFactory.buildPersisted(getTestName(info), studyEnvBundle.getStudyEnv());
        ParticipantFile undownloadedFile = participantFileFactory.buildPersisted(noDownload);
        surveyResponseFactory.buildWithAnswers(noDownload, survey, Map.of(
                "my_question", Answer.builder()
                        .format(AnswerFormat.FILE_UPLOAD)
                        .objectValue("[{\"fileName\":\"%s\"}]".formatted(undownloadedFile.getFileName()))
                        .build()));

        // enrollee with no file at all
        Enrollee noFile = enrolleeFactory.buildPersisted(getTestName(info), studyEnvBundle.getStudyEnv());

        List<EnrolleeSearchExpressionResult> downloadedResults = enrolleeSearchExpressionDao.executeSearch(downloadedExp, studyEnvId);
        List<EnrolleeSearchExpressionResult> notDownloadedResults = enrolleeSearchExpressionDao.executeSearch(notDownloadedExp, studyEnvId);

        assertEquals(1, downloadedResults.size());
        assertTrue(downloadedResults.stream().anyMatch(r -> r.getEnrollee().getId().equals(withDownload.getId())));

        assertEquals(2, notDownloadedResults.size());
        assertTrue(notDownloadedResults.stream().anyMatch(r -> r.getEnrollee().getId().equals(noDownload.getId())));
        assertTrue(notDownloadedResults.stream().anyMatch(r -> r.getEnrollee().getId().equals(noFile.getId())));

        // Include.participantFiles attaches files with answers and downloads to each result
        List<EnrolleeSearchExpressionResult> withFilesIncluded = enrolleeSearchExpressionDao.executeSearch(
                downloadedExp, studyEnvId,
                EnrolleeSearchOptions.builder().includes(List.of(EnrolleeSearchOptions.Include.participantFiles)).build());
        assertEquals(1, withFilesIncluded.size());
        EnrolleeSearchExpressionResult result = withFilesIncluded.get(0);
        assertEquals(1, result.getParticipantFiles().size());
        assertEquals(downloadedFile.getFileName(), result.getParticipantFiles().get(0).getFileName());
        assertEquals(1, result.getParticipantFiles().get(0).getAssociatedAnswers().size());
        assertEquals("my_question", result.getParticipantFiles().get(0).getAssociatedAnswers().get(0).getQuestionStableId());
        assertEquals(1, result.getParticipantFiles().get(0).getDownloads().size());
    }
}
