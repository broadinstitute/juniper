package bio.terra.pearl.core.service.search.expressions;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.factory.survey.SurveyResponseFactory;
import bio.terra.pearl.core.model.address.MailingAddress;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpression;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnrolleeSearchExpressionTest extends BaseSpringBootTest {

    @Autowired
    EnrolleeFactory enrolleeFactory;

    @Autowired
    SurveyFactory surveyFactory;

    @Autowired
    StudyEnvironmentFactory studyEnvironmentFactory;

    @Autowired
    SurveyResponseFactory surveyResponseFactory;

    @Autowired
    EnrolleeSearchExpressionParser enrolleeSearchExpressionParser;

    @Test
    @Transactional
    public void testBasicEvaluate() {
        String rule = "{profile.givenName} = 'John'";
        EnrolleeSearchExpression searchExp = enrolleeSearchExpressionParser.parseRule(rule);

        assertFalse(searchExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(Enrollee.builder().build())
                .profile(Profile.builder().build())
                .build()));

        assertTrue(searchExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(Enrollee.builder().build())
                .profile(Profile.builder().givenName("John").build())
                .build()));

        assertFalse(searchExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(Enrollee.builder().build())
                .profile(Profile.builder().givenName("Jonas").build())
                .build()));
    }

    @Test
    @Transactional
    public void testBooleanOperators() {
        String andRule = "{profile.givenName} = 'John' and {profile.familyName} = 'Doe'";
        String orRule = "{profile.givenName} = 'John' or {profile.familyName} = 'Doe'";
        EnrolleeSearchExpression andExp = enrolleeSearchExpressionParser.parseRule(andRule);
        EnrolleeSearchExpression orExp = enrolleeSearchExpressionParser.parseRule(orRule);

        assertFalse(andExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(Enrollee.builder().build())
                .profile(Profile.builder().build())
                .build()));

        assertFalse(orExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(Enrollee.builder().build())
                .profile(Profile.builder().build())
                .build()));

        assertFalse(andExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(Enrollee.builder().build())
                .profile(Profile.builder().givenName("John").build())
                .build()));

        assertTrue(orExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(Enrollee.builder().build())
                .profile(Profile.builder().givenName("John").build())
                .build()));

        assertTrue(andExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(Enrollee.builder().build())
                .profile(Profile.builder().givenName("John").familyName("Doe").build())
                .build()));

        assertTrue(orExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(Enrollee.builder().build())
                .profile(Profile.builder().givenName("John").familyName("Doe").build())
                .build()));

        assertTrue(orExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(Enrollee.builder().build())
                .profile(Profile.builder().givenName("Something Else").familyName("Doe").build())
                .build()));
    }

    @Test
    @Transactional
    public void testAnswerEvaluate(TestInfo info) {
        StudyEnvironment studyEnvironment = studyEnvironmentFactory.buildPersisted(getTestName(info));

        Survey survey = surveyFactory.buildPersisted(getTestName(info));
        surveyFactory.attachToEnv(survey, studyEnvironment.getId(), true);

        Enrollee enrolleeMatches = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        surveyResponseFactory.buildWithAnswers(enrolleeMatches, survey, Map.of(
                "oh_oh_givenName", "John"
        ));
        Enrollee enrolleeDoesNotMatch = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        surveyResponseFactory.buildWithAnswers(enrolleeDoesNotMatch, survey, Map.of(
                "oh_oh_givenName", "jonas"
        ));
        Enrollee enrolleeNoResponse = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);


        String rule = "{answer.%s.oh_oh_givenName} = 'John'".formatted(survey.getStableId());
        EnrolleeSearchExpression searchExp = enrolleeSearchExpressionParser.parseRule(rule);

        assertTrue(searchExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(enrolleeMatches)
                .build()));

        assertFalse(searchExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(enrolleeDoesNotMatch)
                .build()));

        assertFalse(searchExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(enrolleeNoResponse)
                .build()));
    }


    @Test
    @Transactional
    public void testEvaluateMailingAddress() {
        String rule = "{profile.mailingAddress.state} = 'MA'";
        EnrolleeSearchExpression searchExp = enrolleeSearchExpressionParser.parseRule(rule);

        assertFalse(searchExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(Enrollee.builder().build())
                .profile(Profile.builder().build())
                .build()));

        assertTrue(searchExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(Enrollee.builder().build())
                .profile(Profile.builder().mailingAddress(MailingAddress.builder().state("MA").build()).build())
                .build()));

        assertFalse(searchExp.evaluate(EnrolleeSearchContext
                .builder()
                .enrollee(Enrollee.builder().build())
                .profile(Profile.builder().mailingAddress(MailingAddress.builder().state("NY").build()).build())
                .build()));
    }


}