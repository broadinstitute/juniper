package bio.terra.pearl.core.service.search.sql;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.service.search.BooleanOperator;
import bio.terra.pearl.core.service.search.ComparisonOperator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class SQLSearchTest extends BaseSpringBootTest {

    UUID fakeStudyEnvId = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Test
    void testBasicQueryGeneration() {
        SQLSearch sqlSearch = new SQLSearch(fakeStudyEnvId);
        sqlSearch.setSqlWhereClause(new SQLWhereComparisonExpression(
                new SQLWhereFieldTerm("enrollee", "id"),
                new SQLWhereValueTerm(123),
                ComparisonOperator.EQUALS
        ));

        // :0 is expected, as it is a bound paramater and should not be present unsanitized in the raw query
        Assertions.assertEquals("SELECT enrollee.* FROM enrollee enrollee  WHERE (enrollee.id = :0)" +
                        " AND enrollee.study_environment_id = :studyEnvironmentId",
                sqlSearch.generateQueryString());
    }

    @Test
    void testComplexJoinsAndSelectsQueryGeneration() {
        SQLSearch sqlSearch = new SQLSearch(fakeStudyEnvId);
        sqlSearch.addSelectClause(new SQLSelectClause("profile", "given_name"));
        sqlSearch.addJoinClause(new SQLJoinClause("profile", "profile", "enrollee.profile_id = profile.id"));
        sqlSearch.setSqlWhereClause(new SQLWhereComparisonExpression(
                new SQLWhereFieldTerm("profile", "given_name"),
                new SQLWhereValueTerm("John"),
                ComparisonOperator.NOT_EQUALS
        ));

        Assertions.assertEquals("SELECT enrollee.*, profile.given_name FROM enrollee enrollee INNER JOIN " +
                        "profile profile ON enrollee.profile_id = profile.id WHERE (profile.given_name != :0)" +
                        " AND enrollee.study_environment_id = :studyEnvironmentId",
                sqlSearch.generateQueryString());
    }

    @Test
    void testNestedWhereQueries() {
        SQLSearch sqlSearch = new SQLSearch(fakeStudyEnvId);
        sqlSearch.addSelectClause(new SQLSelectClause("profile", "given_name"));
        sqlSearch.addSelectClause(new SQLSelectClause("profile", "family_name"));
        sqlSearch.addJoinClause(new SQLJoinClause("profile", "profile", "enrollee.profile_id = profile.id"));
        sqlSearch.setSqlWhereClause(new SQLWhereBooleanExpression(new SQLWhereComparisonExpression(
                new SQLWhereFieldTerm("profile", "given_name"),
                new SQLWhereValueTerm("Jonas"),
                ComparisonOperator.EQUALS
        ), new SQLWhereBooleanExpression(new SQLWhereComparisonExpression(
                new SQLWhereFieldTerm("profile", "family_name"),
                new SQLWhereValueTerm("Salk"),
                ComparisonOperator.EQUALS
        ), new SQLWhereComparisonExpression(
                new SQLWhereFieldTerm("enrollee", "id"),
                new SQLWhereValueTerm(123),
                ComparisonOperator.EQUALS
        ), BooleanOperator.AND), BooleanOperator.AND));

        Assertions.assertEquals(
                "SELECT enrollee.*, profile.given_name, profile.family_name FROM enrollee enrollee " +
                        "INNER JOIN profile profile ON enrollee.profile_id = profile.id " +
                        "WHERE ((profile.given_name = :0) AND ((profile.family_name = :1) AND (enrollee.id = :2)))" +
                        " AND enrollee.study_environment_id = :studyEnvironmentId",
                sqlSearch.generateQueryString());
    }
}