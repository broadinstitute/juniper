package bio.terra.pearl.core.service.search.sql;

import bio.terra.pearl.core.BaseSpringBootTest;
import org.jooq.Query;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.jooq.impl.DSL.condition;

class EnrolleeSearchQueryBuilderTest extends BaseSpringBootTest {

    UUID fakeStudyEnvId = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Test
    void testBasicQueryGeneration() {
        EnrolleeSearchQueryBuilder enrolleeSearchQueryBuilder = new EnrolleeSearchQueryBuilder(fakeStudyEnvId);
        enrolleeSearchQueryBuilder.addCondition(condition("enrollee.id = ?", 123));

        Query query = enrolleeSearchQueryBuilder.toQuery(DSL.using(SQLDialect.POSTGRES));

        Assertions.assertEquals("select * from enrollee enrollee " +
                        "where ((enrollee.id = ?) and (enrollee.study_environment_id = ?))",
                query.getSQL());

        Assertions.assertEquals(123, query.getBindValues().get(0));
        Assertions.assertEquals(fakeStudyEnvId, query.getBindValues().get(1));
    }

    @Test
    void testComplexJoinsAndSelectsQueryGeneration() {
        EnrolleeSearchQueryBuilder enrolleeSearchQueryBuilder = new EnrolleeSearchQueryBuilder(fakeStudyEnvId);
        enrolleeSearchQueryBuilder.addSelectClause(new SQLSelectClause("profile", "given_name"));
        enrolleeSearchQueryBuilder.addJoinClause(new SQLJoinClause("profile", "profile", "enrollee.profile_id = profile.id"));
        enrolleeSearchQueryBuilder.addCondition(condition("profile.given_name != ?", "Jonas"));

        Query query = enrolleeSearchQueryBuilder.toQuery(DSL.using(SQLDialect.POSTGRES));
        Assertions.assertEquals("select *, profile.given_name from enrollee enrollee " +
                        "join profile profile on (enrollee.profile_id = profile.id) " +
                        "where ((profile.given_name != ?) and (enrollee.study_environment_id = ?))",
                query.getSQL());

        Assertions.assertEquals("Jonas", query.getBindValues().get(0));
        Assertions.assertEquals(fakeStudyEnvId, query.getBindValues().get(1));
    }
}