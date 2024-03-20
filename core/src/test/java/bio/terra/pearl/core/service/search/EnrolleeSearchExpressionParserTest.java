package bio.terra.pearl.core.service.search;

import bio.terra.pearl.core.BaseSpringBootTest;
import org.jooq.Query;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnrolleeSearchExpressionParserTest extends BaseSpringBootTest {

    UUID fakeStudyEnvId = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Autowired
    EnrolleeSearchExpressionParser enrolleeSearchExpressionParser;

    @Test
    public void testParseRule() {
        String rule = "{profile.givenName} = 'John'";
        EnrolleeSearchExpression searchExp = enrolleeSearchExpressionParser.parseRule(rule);

        Query query = searchExp.generateQuery(fakeStudyEnvId);
        assertEquals("select enrollee.*, profile.given_name from enrollee enrollee " +
                        "left outer join profile profile on (enrollee.profile_id = profile.id) " +
                        "where ((profile.given_name = ?) and (enrollee.study_environment_id = ?))",
                query.getSQL());

        assertEquals("John", query.getBindValues().get(0));
        assertEquals(fakeStudyEnvId, query.getBindValues().get(1));
    }

    @Test
    public void testNestedParsing() {
        String rule = "{profile.givenName} = 'John' and {profile.familyName} = 'Doe'";
        EnrolleeSearchExpression searchExp = enrolleeSearchExpressionParser.parseRule(rule);

        Query query = searchExp.generateQuery(fakeStudyEnvId);
        assertEquals("select enrollee.*, profile.given_name, profile.family_name from enrollee enrollee " +
                        "left outer join profile profile on (enrollee.profile_id = profile.id) " +
                        "where ((profile.family_name = ?) and (profile.given_name = ?) " +
                        "and (enrollee.study_environment_id = ?))",
                query.getSQL());

        assertEquals(3, query.getBindValues().size());
        assertEquals("Doe", query.getBindValues().get(0));
        assertEquals("John", query.getBindValues().get(1));
        assertEquals(fakeStudyEnvId, query.getBindValues().get(2));
    }

    @Test
    public void testComplexParsing() {
        String rule = "{profile.givenName} = 'John' and {answer.oh_oh_basics.oh_oh_givenName} = 'John'";
        EnrolleeSearchExpression searchExp = enrolleeSearchExpressionParser.parseRule(rule);

        Query query = searchExp.generateQuery(fakeStudyEnvId);
        assertEquals("select enrollee.*, profile.given_name, oh_oh_givenName.string_value, " +
                        "oh_oh_givenName.question_stable_id, oh_oh_givenName.survey_stable_id " +
                        "from enrollee enrollee " +
                        "left outer join profile profile on (enrollee.profile_id = profile.id) " +
                        "left outer join answer oh_oh_givenName on (enrollee.id = oh_oh_givenName.enrollee_id) " +
                        "where ((oh_oh_givenName.survey_stable_id = ? AND oh_oh_givenName.question_stable_id = ?) " +
                        "and (oh_oh_givenName.string_value = ?) and (profile.given_name = ?) " +
                        "and (enrollee.study_environment_id = ?))",
                query.getSQL());

        assertEquals(5, query.getBindValues().size());
        assertEquals("oh_oh_basics", query.getBindValues().get(0));
        assertEquals("oh_oh_givenName", query.getBindValues().get(1));
        assertEquals("John", query.getBindValues().get(2));
        assertEquals("John", query.getBindValues().get(3));
        assertEquals(fakeStudyEnvId, query.getBindValues().get(4));
    }

    @Test
    public void testSanitizesAnswerName() {

        // parses ok with normal stable id
        enrolleeSearchExpressionParser.parseRule("{answer.oh_oh_basics.question_stable_id} = 2");

        // does not parse ok with invalid stable id
        assertThrows(IllegalArgumentException.class,
                () -> enrolleeSearchExpressionParser.parseRule("{answer.oh_oh_basics.SELECT * FROM enrollee} = 2"));

        assertThrows(IllegalArgumentException.class,
                () -> enrolleeSearchExpressionParser.parseRule("{answer.SELECT * FROM enrollee.oh_oh_givenName} = 2"));

    }

    @Test
    public void testParseAge() {
        EnrolleeSearchExpression exp = enrolleeSearchExpressionParser.parseRule("{age} > 18");

        Query query = exp.generateQuery(fakeStudyEnvId);
        assertEquals("select enrollee.*, profile.birth_date from enrollee enrollee " +
                        "left outer join profile profile on (enrollee.profile_id = profile.id ) " +
                        "where ((EXTRACT('YEAR' FROM AGE(profile.birth_date)) > ?) " +
                        "and (enrollee.study_environment_id = ?))",
                query.getSQL());

        assertEquals(2, query.getBindValues().size());
        assertEquals(18.0, query.getBindValues().get(0));
        assertEquals(fakeStudyEnvId, query.getBindValues().get(1));
    }

    @Test
    public void testParseMailingAddress() {
        EnrolleeSearchExpression exp = enrolleeSearchExpressionParser.parseRule("{profile.mailingAddress.state} = 'CA'");

        Query query = exp.generateQuery(fakeStudyEnvId);
        assertEquals("select enrollee.*, mailing_address.state " +
                        "from enrollee enrollee " +
                        "left outer join profile profile on (enrollee.profile_id = profile.id) " +
                        "left outer join mailing_address mailing_address on (profile.mailing_address_id = mailing_address.id) " +
                        "where ((mailing_address.state = ?) and (enrollee.study_environment_id = ?))",
                query.getSQL());
    }

}