package bio.terra.pearl.core.dao.search;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.search.BooleanOperator;
import bio.terra.pearl.core.service.search.ComparisonOperator;
import bio.terra.pearl.core.service.search.sql.SQLJoinClause;
import bio.terra.pearl.core.service.search.sql.SQLSearch;
import bio.terra.pearl.core.service.search.sql.SQLSelectClause;
import bio.terra.pearl.core.service.search.sql.SQLWhereBooleanExpression;
import bio.terra.pearl.core.service.search.sql.SQLWhereComparisonExpression;
import bio.terra.pearl.core.service.search.sql.SQLWhereField;
import bio.terra.pearl.core.service.search.sql.SQLWhereValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

class EnrolleeSearchExpressionDaoTest extends BaseSpringBootTest {

    @Autowired
    EnrolleeSearchExpressionDao enrolleeSearchExpressionDao;

    @Autowired
    EnrolleeFactory enrolleeFactory;

    @Autowired
    StudyEnvironmentFactory studyEnvironmentFactory;

    @Test
    @Transactional
    void testExecuteBasicSearch(TestInfo info) {
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(getTestName(info));
        StudyEnvironment otherEnv = studyEnvironmentFactory.buildPersisted(getTestName(info));

        SQLSearch sqlSearch = new SQLSearch(studyEnv.getId());
        sqlSearch.addSelectClause(new SQLSelectClause("profile", "given_name"));
        sqlSearch.addSelectClause(new SQLSelectClause("profile", "family_name"));
        sqlSearch.addJoinClause(new SQLJoinClause("profile", "profile", "enrollee.profile_id = profile.id"));
        sqlSearch.setSqlWhereClause(new SQLWhereBooleanExpression(new SQLWhereComparisonExpression(
                new SQLWhereField("profile", "given_name"),
                new SQLWhereValue("Jonas"),
                ComparisonOperator.EQUALS
        ),new SQLWhereComparisonExpression(
                new SQLWhereField("profile", "family_name"),
                new SQLWhereValue("Salk"),
                ComparisonOperator.EQUALS
        ), BooleanOperator.AND));

        // does not bind user inputted parameters (SQLWhereValues)
        Assertions.assertEquals(
                "SELECT enrollee.*, profile.given_name, profile.family_name FROM enrollee enrollee " +
                        "INNER JOIN profile profile ON enrollee.profile_id = profile.id " +
                        "WHERE ((profile.given_name = :0) AND (profile.family_name = :1)) " +
                        "AND enrollee.study_environment_id = :studyEnvironmentId",
                sqlSearch.generateQueryString());

        Assertions.assertEquals(0, enrolleeSearchExpressionDao.executeSearch(sqlSearch).size());

        Enrollee salk1 = enrolleeFactory.buildPersisted(
                getTestName(info),
                studyEnv,
                Profile.builder().givenName("Jonas").familyName("Salk").build());
        Enrollee salk2 = enrolleeFactory.buildPersisted(
                getTestName(info),
                studyEnv,
                Profile.builder().givenName("Jonas").familyName("Salk").build());
        Enrollee notSalk = enrolleeFactory.buildPersisted(
                getTestName(info),
                studyEnv,
                Profile.builder().givenName("Jonas").familyName("Smith").build());
        Enrollee salk3NotInEnv = enrolleeFactory.buildPersisted(
                getTestName(info),
                otherEnv,
                Profile.builder().givenName("Jonas").familyName("Salk").build());


        List<Enrollee> results = enrolleeSearchExpressionDao.executeSearch(sqlSearch);

        Assertions.assertEquals(2, results.size());
        Assertions.assertTrue(results.contains(salk1));
        Assertions.assertTrue(results.contains(salk2));
        Assertions.assertFalse(results.contains(salk3NotInEnv));
        Assertions.assertFalse(results.contains(notSalk));

    }
}