package bio.terra.pearl.core.service.search;

import bio.terra.pearl.core.BaseSpringBootTest;
import org.jooq.Query;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnrolleeSearchExpressionParserTest extends BaseSpringBootTest {

    UUID fakeStudyEnvId = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Autowired
    EnrolleeSearchExpressionParser enrolleeSearchExpressionParser;

    @Test
    public void testParseIntoSQL() {
        String rule = "{age} > 18 and {answer.basics.diagnosis} = 'something' and ({profile.mailingAddress.country} = 'gb' or {profile.mailingAddress.country} = 'us')";
        EnrolleeSearchExpression searchExp = enrolleeSearchExpressionParser.parseRule(rule);

        Query query = searchExp.generateQuery(fakeStudyEnvId);
        assertEquals("""
                        select enrollee.participant_user_id as enrollee_participant_user_id, \
                        enrollee.profile_id as enrollee_profile_id, \
                        enrollee.study_environment_id as enrollee_study_environment_id, \
                        enrollee.pre_enrollment_response_id as enrollee_pre_enrollment_response_id, \
                        enrollee.shortcode as enrollee_shortcode, \
                        enrollee.subject as enrollee_subject, \
                        enrollee.consented as enrollee_consented, \
                        enrollee.source as enrollee_source, \
                        enrollee.id as enrollee_id, \
                        enrollee.created_at as enrollee_created_at, \
                        enrollee.last_updated_at as enrollee_last_updated_at, \
                        profile.given_name as profile_given_name, \
                        profile.family_name as profile_family_name, \
                        profile.mailing_address_id as profile_mailing_address_id, \
                        profile.preferred_language as profile_preferred_language, \
                        profile.contact_email as profile_contact_email, \
                        profile.do_not_email as profile_do_not_email, \
                        profile.do_not_email_solicit as profile_do_not_email_solicit, \
                        profile.birth_date as profile_birth_date, \
                        profile.phone_number as profile_phone_number, \
                        profile.sex_at_birth as profile_sex_at_birth, \
                        profile.id as profile_id, \
                        profile.created_at as profile_created_at, \
                        profile.last_updated_at as profile_last_updated_at, \
                        answer_diagnosis.creating_admin_user_id as answer_diagnosis_creating_admin_user_id, \
                        answer_diagnosis.creating_participant_user_id as answer_diagnosis_creating_participant_user_id, \
                        answer_diagnosis.survey_response_id as answer_diagnosis_survey_response_id, \
                        answer_diagnosis.enrollee_id as answer_diagnosis_enrollee_id, \
                        answer_diagnosis.question_stable_id as answer_diagnosis_question_stable_id, \
                        answer_diagnosis.survey_stable_id as answer_diagnosis_survey_stable_id, \
                        answer_diagnosis.other_description as answer_diagnosis_other_description, \
                        answer_diagnosis.survey_version as answer_diagnosis_survey_version, \
                        answer_diagnosis.viewed_language as answer_diagnosis_viewed_language, \
                        answer_diagnosis.answer_type as answer_diagnosis_answer_type, \
                        answer_diagnosis.string_value as answer_diagnosis_string_value, \
                        answer_diagnosis.object_value as answer_diagnosis_object_value, \
                        answer_diagnosis.number_value as answer_diagnosis_number_value, \
                        answer_diagnosis.boolean_value as answer_diagnosis_boolean_value, \
                        answer_diagnosis.id as answer_diagnosis_id, \
                        answer_diagnosis.created_at as answer_diagnosis_created_at, \
                        answer_diagnosis.last_updated_at as answer_diagnosis_last_updated_at, \
                        mailing_address.street1 as mailing_address_street1, \
                        mailing_address.street2 as mailing_address_street2, \
                        mailing_address.state as mailing_address_state, \
                        mailing_address.country as mailing_address_country, \
                        mailing_address.city as mailing_address_city, \
                        mailing_address.postal_code as mailing_address_postal_code, \
                        mailing_address.id as mailing_address_id, \
                        mailing_address.created_at as mailing_address_created_at, \
                        mailing_address.last_updated_at as mailing_address_last_updated_at \
                        from enrollee enrollee \
                        left outer join profile profile on (enrollee.profile_id = profile.id) \
                        left outer join answer answer_diagnosis on (enrollee.id = answer_diagnosis.enrollee_id) \
                        left outer join mailing_address mailing_address on (profile.mailing_address_id = mailing_address.id) \
                        where (((mailing_address.country = ?) or (mailing_address.country = ?)) \
                        and (answer_diagnosis.survey_stable_id = ? \
                        AND answer_diagnosis.question_stable_id = ?) \
                        and (answer_diagnosis.string_value = ?) \
                        and (EXTRACT('YEAR' FROM AGE(profile.birth_date)) > ?) \
                        and (enrollee.study_environment_id = ?))\
                        """,
                query.getSQL());

        assertEquals(7, query.getBindValues().size());
        assertEquals("us", query.getBindValues().get(0));
        assertEquals("gb", query.getBindValues().get(1));
        assertEquals("basics", query.getBindValues().get(2));
        assertEquals("diagnosis", query.getBindValues().get(3));
        assertEquals("something", query.getBindValues().get(4));
        assertEquals(18.0, query.getBindValues().get(5));
        assertEquals(fakeStudyEnvId, query.getBindValues().get(6));
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

    @Test
    @Transactional
    public void testFunctionsErrorWithWrongType() {
        assertThrows(IllegalArgumentException.class,
                () -> enrolleeSearchExpressionParser.parseRule(
                        "lower({enrollee.subject}) = 'true'"
                ));

        assertThrows(IllegalArgumentException.class,
                () -> enrolleeSearchExpressionParser.parseRule(
                        "trim(10) = 10"
                ));

        assertThrows(IllegalArgumentException.class,
                () -> enrolleeSearchExpressionParser.parseRule(
                        "max('hey', 3, 4) = 5"
                ));

        assertThrows(IllegalArgumentException.class,
                () -> enrolleeSearchExpressionParser.parseRule(
                        "min('hey', 3, 4) = 5"
                ));
    }


}
