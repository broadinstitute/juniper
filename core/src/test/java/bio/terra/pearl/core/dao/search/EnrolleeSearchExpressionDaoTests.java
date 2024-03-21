package bio.terra.pearl.core.dao.search;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.factory.survey.SurveyResponseFactory;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.search.EnrolleeSearchResult;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpression;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

        List<EnrolleeSearchResult> results = enrolleeSearchExpressionDao.executeSearch(exp, studyEnv.getId());

        Assertions.assertEquals(1, results.size());
        Assertions.assertTrue(results.stream().anyMatch(r -> r.getEnrollee().getId().equals(salk.getId())));
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

        List<EnrolleeSearchResult> results = enrolleeSearchExpressionDao.executeSearch(exp, studyEnv.getId());

        Assertions.assertEquals(1, results.size());

        EnrolleeSearchResult result = results.get(0);

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

        List<EnrolleeSearchResult> results = enrolleeSearchExpressionDao.executeSearch(exp, studyEnv.getId());

        Assertions.assertEquals(1, results.size());
        EnrolleeSearchResult result = results.get(0);
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


        List<EnrolleeSearchResult> results = enrolleeSearchExpressionDao.executeSearch(exp, studyEnv.getId());

        Assertions.assertEquals(3, results.size());

        Assertions.assertTrue(results.stream().anyMatch(r -> r.getEnrollee().getId().equals(e1.getId())));
        Assertions.assertTrue(results.stream().anyMatch(r -> r.getEnrollee().getId().equals(e2.getId())));
        Assertions.assertTrue(results.stream().anyMatch(r -> r.getEnrollee().getId().equals(e3.getId())));
    }
}