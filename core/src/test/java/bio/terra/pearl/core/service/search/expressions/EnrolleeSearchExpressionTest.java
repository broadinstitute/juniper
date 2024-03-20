package bio.terra.pearl.core.service.search.expressions;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.survey.AnswerFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.factory.survey.SurveyResponseFactory;
import bio.terra.pearl.core.service.survey.AnswerService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

class EnrolleeSearchExpressionTest extends BaseSpringBootTest {

    UUID fakeStudyEnvId = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Autowired
    AnswerService answerService;

    @Autowired
    AnswerFactory answerFactory;

    @Autowired
    EnrolleeFactory enrolleeFactory;

    @Autowired
    SurveyFactory surveyFactory;

    @Autowired
    SurveyResponseFactory surveyResponseFactory;
//
//    @Test
//    public void testBasicSearchExpressionToSQL() {
//        EnrolleeSearchExpression expression = new BooleanSearchExpression(
//                new EnrolleeTermComparisonFacet(
//                        new ProfileTerm("givenName"),
//                        new UserInputTerm(new SearchValue("John")),
//                        ComparisonOperator.EQUALS),
//                new BooleanSearchExpression(
//                        new EnrolleeTermComparisonFacet(
//                                new ProfileTerm("familyName"),
//                                new UserInputTerm(new SearchValue("Salk")),
//                                ComparisonOperator.EQUALS),
//                        new EnrolleeTermComparisonFacet(
//                                new UserInputTerm(new SearchValue(124.)),
//                                new UserInputTerm(new SearchValue(123.)),
//                                ComparisonOperator.GREATER_THAN),
//                        Operator.AND),
//                Operator.OR);
//
//        EnrolleeSearchQueryBuilder enrolleeSearchQueryBuilder = expression.generateQueryBuilder(fakeStudyEnvId);
//        Query query = enrolleeSearchQueryBuilder.toQuery(DSL.using(SQLDialect.POSTGRES));
//
//        assertEquals("select enrollee.*, profile.given_name, profile.family_name from enrollee enrollee " +
//                        "left outer join profile profile on (enrollee.profile_id = profile.id) " +
//                        "where ((((? > ?) and (profile.family_name = ?)) " +
//                        "or (profile.given_name = ?)) " +
//                        "and (enrollee.study_environment_id = ?))",
//                query.getSQL());
//
//        assertEquals(5, query.getBindValues().size());
//        assertEquals(124., query.getBindValues().get(0));
//        assertEquals(123., query.getBindValues().get(1));
//        assertEquals("Salk", query.getBindValues().get(2));
//        assertEquals("John", query.getBindValues().get(3));
//        assertEquals(fakeStudyEnvId, query.getBindValues().get(4));
//
//    }
//
//    @Test
//    public void testBasicSearchExpressionEvaluate() {
//        EnrolleeSearchExpression expression = new BooleanSearchExpression(
//                new EnrolleeTermComparisonFacet(
//                        new ProfileTerm("givenName"),
//                        new UserInputTerm(new SearchValue("Jonas")),
//                        ComparisonOperator.EQUALS),
//                new EnrolleeTermComparisonFacet(
//                        new ProfileTerm("familyName"),
//                        new UserInputTerm(new SearchValue("Salk")),
//                        ComparisonOperator.EQUALS),
//                Operator.AND);
//
//        EnrolleeSearchContext enrolleeCtx = new EnrolleeSearchContext();
//
//        enrolleeCtx.setEnrollee(
//                new Enrollee()
//        );
//
//        enrolleeCtx.setProfile(
//                Profile.builder().givenName("Jonas").familyName("Salk").build()
//        );
//
//        assertTrue(expression.evaluate(enrolleeCtx));
//
//        enrolleeCtx.getProfile().setGivenName("John");
//
//        assertFalse(expression.evaluate(enrolleeCtx));
//
//        enrolleeCtx.getProfile().setGivenName("Jonas");
//        enrolleeCtx.getProfile().setFamilyName("Smith");
//
//        assertFalse(expression.evaluate(enrolleeCtx));
//    }
//
//    @Test
//    @Transactional
//    public void testAnswerSearchExpressionEvaluate(TestInfo info) {
//        Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info));
//        Survey survey = surveyFactory.buildPersisted(getTestName(info));
//        surveyResponseFactory.buildWithAnswers(
//                enrollee,
//                survey,
//                Map.of("test_question", "some_value"));
//
//        EnrolleeSearchExpression expression1 =
//                new EnrolleeTermComparisonFacet(
//                        new AnswerTerm(answerService, survey.getStableId(), "test_question"),
//                        new UserInputTerm(new SearchValue("some_value")),
//                        ComparisonOperator.EQUALS);
//
//        EnrolleeSearchExpression expression2 =
//                new EnrolleeTermComparisonFacet(
//                        new AnswerTerm(answerService, survey.getStableId(), "test_question"),
//                        new UserInputTerm(new SearchValue("diff_value")),
//                        ComparisonOperator.EQUALS);
//
//        EnrolleeSearchContext enrolleeCtx = new EnrolleeSearchContext();
//        enrolleeCtx.setEnrollee(enrollee);
//
//        assertTrue(expression1.evaluate(enrolleeCtx));
//        assertFalse(expression2.evaluate(enrolleeCtx));
//    }

}