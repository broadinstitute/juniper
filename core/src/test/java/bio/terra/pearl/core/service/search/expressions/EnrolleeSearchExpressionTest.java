package bio.terra.pearl.core.service.search.expressions;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.service.search.BooleanOperator;
import bio.terra.pearl.core.service.search.ComparisonOperator;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.terms.ConstantTermExtractor;
import bio.terra.pearl.core.service.search.terms.ProfileTermExtractor;
import bio.terra.pearl.core.service.search.terms.Term;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnrolleeSearchExpressionTest extends BaseSpringBootTest {

    UUID fakeStudyEnvId = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Test
    public void testBasicSearchExpressionToSQL() {
        EnrolleeSearchExpression expression = new BooleanSearchExpression(
                new EnrolleeSearchFacet(
                        new ProfileTermExtractor(ProfileTermExtractor.ProfileField.GIVEN_NAME),
                        new ConstantTermExtractor(new Term("John")),
                        ComparisonOperator.EQUALS),
                new BooleanSearchExpression(
                        new EnrolleeSearchFacet(
                                new ProfileTermExtractor(ProfileTermExtractor.ProfileField.FAMILY_NAME),
                                new ConstantTermExtractor(new Term("Salk")),
                                ComparisonOperator.EQUALS),
                        new EnrolleeSearchFacet(
                                new ConstantTermExtractor(new Term(124.)),
                                new ConstantTermExtractor(new Term(123.)),
                                ComparisonOperator.GREATER_THAN),
                        BooleanOperator.AND),
                BooleanOperator.OR);

        assertEquals("SELECT enrollee.*, profile.given_name, profile.family_name FROM enrollee enrollee " +
                        "INNER JOIN profile profile ON enrollee.profile_id = profile.id " +
                        "WHERE ((profile.given_name = :0) OR ((profile.family_name = :1) AND (:2 > :3)))" +
                        " AND enrollee.study_environment_id = :studyEnvironmentId",
                expression.generateSqlSearch(fakeStudyEnvId).generateQueryString());
    }

    @Test
    public void testBasicSearchExpressionEvaluate() {
        EnrolleeSearchExpression expression = new BooleanSearchExpression(
                new EnrolleeSearchFacet(
                        new ProfileTermExtractor(ProfileTermExtractor.ProfileField.GIVEN_NAME),
                        new ConstantTermExtractor(new Term("Jonas")),
                        ComparisonOperator.EQUALS),
                new EnrolleeSearchFacet(
                        new ProfileTermExtractor(ProfileTermExtractor.ProfileField.FAMILY_NAME),
                        new ConstantTermExtractor(new Term("Salk")),
                        ComparisonOperator.EQUALS),
                BooleanOperator.AND);

        EnrolleeSearchContext enrolleeCtx = new EnrolleeSearchContext();

        enrolleeCtx.setEnrollee(
                new Enrollee()
        );

        enrolleeCtx.setProfile(
                Profile.builder().givenName("Jonas").familyName("Salk").build()
        );

        assertTrue(expression.evaluate(enrolleeCtx));

        enrolleeCtx.getProfile().setGivenName("John");

        assertFalse(expression.evaluate(enrolleeCtx));

        enrolleeCtx.getProfile().setGivenName("Jonas");
        enrolleeCtx.getProfile().setFamilyName("Smith");

        assertFalse(expression.evaluate(enrolleeCtx));
    }
}