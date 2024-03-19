package bio.terra.pearl.core.service.search.expressions;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.survey.AnswerFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.factory.survey.SurveyResponseFactory;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.search.ComparisonOperator;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import bio.terra.pearl.core.service.search.terms.AnswerTermExtractor;
import bio.terra.pearl.core.service.search.terms.ConstantTermExtractor;
import bio.terra.pearl.core.service.search.terms.ProfileTermExtractor;
import bio.terra.pearl.core.service.search.terms.Term;
import bio.terra.pearl.core.service.survey.AnswerService;
import org.jooq.Operator;
import org.jooq.Query;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    public void testBasicSearchExpressionToSQL() {
        EnrolleeSearchExpression expression = new BooleanSearchExpression(
                new EnrolleeSearchFacet(
                        new ProfileTermExtractor("givenName"),
                        new ConstantTermExtractor(new Term("John")),
                        ComparisonOperator.EQUALS),
                new BooleanSearchExpression(
                        new EnrolleeSearchFacet(
                                new ProfileTermExtractor("familyName"),
                                new ConstantTermExtractor(new Term("Salk")),
                                ComparisonOperator.EQUALS),
                        new EnrolleeSearchFacet(
                                new ConstantTermExtractor(new Term(124.)),
                                new ConstantTermExtractor(new Term(123.)),
                                ComparisonOperator.GREATER_THAN),
                        Operator.AND),
                Operator.OR);

        EnrolleeSearchQueryBuilder enrolleeSearchQueryBuilder = expression.generateQueryBuilder(fakeStudyEnvId);
        Query query = enrolleeSearchQueryBuilder.toQuery(DSL.using(SQLDialect.POSTGRES));

        assertEquals("select enrollee.*, profile.given_name, profile.family_name from enrollee enrollee " +
                        "join profile profile on (enrollee.profile_id = profile.id) " +
                        "where ((((? > ?) and (profile.family_name = ?)) " +
                        "or (profile.given_name = ?)) " +
                        "and (enrollee.study_environment_id = ?))",
                query.getSQL());

        assertEquals(5, query.getBindValues().size());
        assertEquals(124., query.getBindValues().get(0));
        assertEquals(123., query.getBindValues().get(1));
        assertEquals("Salk", query.getBindValues().get(2));
        assertEquals("John", query.getBindValues().get(3));
        assertEquals(fakeStudyEnvId, query.getBindValues().get(4));

    }

    // TODO
//    @Test
//    public void testBasicSearchExpressionEvaluate() {
//        EnrolleeSearchExpression expression = new BooleanSearchExpression(
//                new EnrolleeSearchFacet(
//                        new ProfileTermExtractor("givenName"),
//                        new ConstantTermExtractor(new Term("Jonas")),
//                        ComparisonOperator.EQUALS),
//                new EnrolleeSearchFacet(
//                        new ProfileTermExtractor("familyName"),
//                        new ConstantTermExtractor(new Term("Salk")),
//                        ComparisonOperator.EQUALS),
//                BooleanOperator.AND);
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

    @Test
    @Transactional
    public void testAnswerSearchExpressionEvaluate(TestInfo info) {
        Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info));
        Survey survey = surveyFactory.buildPersisted(getTestName(info));
        surveyResponseFactory.buildWithAnswers(
                enrollee,
                survey,
                Map.of("test_question", "some_value"));

        EnrolleeSearchExpression expression1 =
                new EnrolleeSearchFacet(
                        new AnswerTermExtractor(answerService, survey.getStableId(), "test_question"),
                        new ConstantTermExtractor(new Term("some_value")),
                        ComparisonOperator.EQUALS);

        EnrolleeSearchExpression expression2 =
                new EnrolleeSearchFacet(
                        new AnswerTermExtractor(answerService, survey.getStableId(), "test_question"),
                        new ConstantTermExtractor(new Term("diff_value")),
                        ComparisonOperator.EQUALS);

        EnrolleeSearchContext enrolleeCtx = new EnrolleeSearchContext();
        enrolleeCtx.setEnrollee(enrollee);

        assertTrue(expression1.evaluate(enrolleeCtx));
        assertFalse(expression2.evaluate(enrolleeCtx));
    }

    // TODO
//    @Test
//    public void testAnswerSearchExpressionSQLGeneration() {
//        EnrolleeSearchExpression expression1 =
//                new EnrolleeSearchFacet(
//                        new AnswerTermExtractor(answerService, "survey_stable_id", "test_question"),
//                        new ConstantTermExtractor(new Term("some_value")),
//                        ComparisonOperator.EQUALS);
//
//        assertEquals(
//                "SELECT enrollee.*, test_question.string_value, " +
//                        "test_question.question_stable_id, test_question.survey_stable_id " +
//                        "FROM enrollee enrollee " +
//                        "INNER JOIN test_question answer " +
//                        "ON enrollee.id = test_question.enrollee_id " +
//                        "WHERE " +
//                        "((test_question.string_value = :0) " +
//                        "AND ((test_question.survey_stable_id = :1) " +
//                        "AND (test_question.question_stable_id = :2))) " +
//                        "AND enrollee.study_environment_id = :studyEnvironmentId",
//                expression1.generateSqlSearch(fakeStudyEnvId).generateQueryString());
//    }
}