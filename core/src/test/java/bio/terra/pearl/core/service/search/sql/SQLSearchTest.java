package bio.terra.pearl.core.service.search.sql;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.service.search.BooleanOperator;
import bio.terra.pearl.core.service.search.ComparisonOperator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SQLSearchTest extends BaseSpringBootTest {

    @Test
    void testBasicQueryGeneration() {
        SQLSearch sqlSearch = new SQLSearch();
        sqlSearch.setSqlWhereClause(new SQLWhereComparisonExpression(
                new SQLWhereField("enrollee", "id"),
                new SQLWhereValue(123),
                ComparisonOperator.EQUALS
        ));

        // :0 is expected, as it is a bound paramater and should not be present unsanitized in the raw query
        Assertions.assertEquals("SELECT enrollee.* FROM enrollee enrollee  WHERE (enrollee.id = :0)",
                sqlSearch.generateQueryString());
    }

    @Test
    void testComplexJoinsAndSelectsQueryGeneration() {
        SQLSearch sqlSearch = new SQLSearch();
        sqlSearch.addSelectClause(new SQLSelectClause("profile", "given_name"));
        sqlSearch.addJoinClause(new SQLJoinClause("profile", "profile", "enrollee.profile_id = profile.id"));
        sqlSearch.setSqlWhereClause(new SQLWhereComparisonExpression(
                new SQLWhereField("profile", "given_name"),
                new SQLWhereValue("John"),
                ComparisonOperator.NOT_EQUALS
        ));

        Assertions.assertEquals("SELECT enrollee.*, profile.given_name FROM enrollee enrollee INNER JOIN " +
                        "profile profile ON enrollee.profile_id = profile.id WHERE (profile.given_name != :0)",
                sqlSearch.generateQueryString());
    }

    @Test
    void testNestedWhereQueries() {
        SQLSearch sqlSearch = new SQLSearch();
        sqlSearch.addSelectClause(new SQLSelectClause("profile", "given_name"));
        sqlSearch.addSelectClause(new SQLSelectClause("profile", "family_name"));
        sqlSearch.addJoinClause(new SQLJoinClause("profile", "profile", "enrollee.profile_id = profile.id"));
        sqlSearch.setSqlWhereClause(new SQLWhereBooleanExpression(new SQLWhereComparisonExpression(
                new SQLWhereField("profile", "given_name"),
                new SQLWhereValue("Jonas"),
                ComparisonOperator.EQUALS
        ), new SQLWhereBooleanExpression(new SQLWhereComparisonExpression(
                new SQLWhereField("profile", "family_name"),
                new SQLWhereValue("Salk"),
                ComparisonOperator.EQUALS
        ), new SQLWhereComparisonExpression(
                new SQLWhereField("enrollee", "id"),
                new SQLWhereValue(123),
                ComparisonOperator.EQUALS
        ), BooleanOperator.AND), BooleanOperator.AND));

        Assertions.assertEquals(
                "SELECT enrollee.*, profile.given_name, profile.family_name FROM enrollee enrollee " +
                        "INNER JOIN profile profile ON enrollee.profile_id = profile.id " +
                        "WHERE ((profile.given_name = :0) AND ((profile.family_name = :1) AND (enrollee.id = :2)))",
                sqlSearch.generateQueryString());
    }
}