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
    public void testParseIntoSQL() {
        String rule = "{profile.givenName} = 'John' and {answer.oh_oh_basics.oh_oh_givenName} = 'John'";
        EnrolleeSearchExpression searchExp = enrolleeSearchExpressionParser.parseRule(rule);

        Query query = searchExp.generateQuery(fakeStudyEnvId);
        assertEquals("select enrollee.consented as enrollee_consented, " +
                        "enrollee.created_at as enrollee_created_at, enrollee.id as enrollee_id, " +
                        "enrollee.last_updated_at as enrollee_last_updated_at, " +
                        "enrollee.participant_user_id as enrollee_participant_user_id, " +
                        "enrollee.pre_enrollment_response_id as enrollee_pre_enrollment_response_id, " +
                        "enrollee.profile_id as enrollee_profile_id, " +
                        "enrollee.shortcode as enrollee_shortcode, " +
                        "enrollee.study_environment_id as enrollee_study_environment_id, " +
                        "enrollee.subject as enrollee_subject, " +
                        "profile.birth_date as profile_birth_date, profile.contact_email as profile_contact_email, " +
                        "profile.created_at as profile_created_at, profile.do_not_email as profile_do_not_email, " +
                        "profile.do_not_email_solicit as profile_do_not_email_solicit, " +
                        "profile.family_name as profile_family_name, " +
                        "profile.given_name as profile_given_name, profile.id as profile_id, " +
                        "profile.last_updated_at as profile_last_updated_at, " +
                        "profile.mailing_address_id as profile_mailing_address_id, " +
                        "profile.phone_number as profile_phone_number, " +
                        "profile.preferred_language as profile_preferred_language, " +
                        "profile.sex_at_birth as profile_sex_at_birth, " +
                        "answer_oh_oh_givenName.answer_type as answer_oh_oh_givenName_answer_type, " +
                        "answer_oh_oh_givenName.boolean_value as answer_oh_oh_givenName_boolean_value, " +
                        "answer_oh_oh_givenName.created_at as answer_oh_oh_givenName_created_at, " +
                        "answer_oh_oh_givenName.creating_admin_user_id as answer_oh_oh_givenName_creating_admin_user_id, " +
                        "answer_oh_oh_givenName.creating_participant_user_id as answer_oh_oh_givenName_creating_participant_user_id, " +
                        "answer_oh_oh_givenName.enrollee_id as answer_oh_oh_givenName_enrollee_id, " +
                        "answer_oh_oh_givenName.id as answer_oh_oh_givenName_id, " +
                        "answer_oh_oh_givenName.last_updated_at as answer_oh_oh_givenName_last_updated_at, " +
                        "answer_oh_oh_givenName.number_value as answer_oh_oh_givenName_number_value, " +
                        "answer_oh_oh_givenName.object_value as answer_oh_oh_givenName_object_value, " +
                        "answer_oh_oh_givenName.other_description as answer_oh_oh_givenName_other_description, " +
                        "answer_oh_oh_givenName.question_stable_id as answer_oh_oh_givenName_question_stable_id, " +
                        "answer_oh_oh_givenName.string_value as answer_oh_oh_givenName_string_value, " +
                        "answer_oh_oh_givenName.survey_response_id as answer_oh_oh_givenName_survey_response_id, " +
                        "answer_oh_oh_givenName.survey_stable_id as answer_oh_oh_givenName_survey_stable_id, " +
                        "answer_oh_oh_givenName.survey_version as answer_oh_oh_givenName_survey_version, " +
                        "answer_oh_oh_givenName.viewed_language as answer_oh_oh_givenName_viewed_language " +
                        "from enrollee enrollee " +
                        "left outer join profile profile on (enrollee.profile_id = profile.id) " +
                        "left outer join answer answer_oh_oh_givenName on (enrollee.id = answer_oh_oh_givenName.enrollee_id) " +
                        "where ((answer_oh_oh_givenName.survey_stable_id = ? AND answer_oh_oh_givenName.question_stable_id = ?) " +
                        "and (answer_oh_oh_givenName.string_value = ?) and (profile.given_name = ?) and (enrollee.study_environment_id = ?))",
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
    public void testInvalidSyntax() {
        assertThrows(IllegalArgumentException.class,
                () -> enrolleeSearchExpressionParser.parseRule("{dasfas} = 2"));

        assertThrows(IllegalArgumentException.class,
                () -> enrolleeSearchExpressionParser.parseRule("{age = 2"));

        assertThrows(IllegalArgumentException.class,
                () -> enrolleeSearchExpressionParser.parseRule("{age} !! 2"));

        assertThrows(IllegalArgumentException.class,
                () -> enrolleeSearchExpressionParser.parseRule("{profile.givenName} = Jonas"));

        assertThrows(IllegalArgumentException.class,
                () -> enrolleeSearchExpressionParser.parseRule("{profile .givenName} = 'Jonas'"));

    }


}