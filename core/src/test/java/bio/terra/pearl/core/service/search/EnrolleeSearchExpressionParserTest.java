package bio.terra.pearl.core.service.search;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.service.search.expressions.EnrolleeSearchExpression;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnrolleeSearchExpressionParserTest extends BaseSpringBootTest {

    UUID fakeStudyEnvId = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Autowired
    EnrolleeSearchExpressionParser enrolleeSearchExpressionParser;

    @Test
    public void testParseRule() {
        String rule = "{profile.givenName} = 'John'";
        EnrolleeSearchExpression searchExp = enrolleeSearchExpressionParser.parseRule(rule);

        assertEquals("SELECT enrollee.*, profile.given_name FROM enrollee enrollee " +
                        "INNER JOIN profile profile ON enrollee.profile_id = profile.id " +
                        "WHERE (profile.given_name = :0) AND enrollee.study_environment_id = :studyEnvironmentId",
                searchExp.generateSqlSearch(fakeStudyEnvId).generateQueryString());
    }

}