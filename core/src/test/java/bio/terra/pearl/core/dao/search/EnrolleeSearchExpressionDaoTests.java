package bio.terra.pearl.core.dao.search;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.factory.survey.SurveyResponseFactory;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.search.EnrolleeSearchResult;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpression;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import org.jooq.Query;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public class EnrolleeSearchExpressionDaoTests extends BaseSpringBootTest {

    @Autowired
    EnrolleeSearchExpressionDao enrolleeSearchExpressionDao;

    @Autowired
    EnrolleeFactory enrolleeFactory;

    @Autowired
    StudyEnvironmentFactory studyEnvironmentFactory;

    @Autowired
    EnrolleeSearchExpressionParser enrolleeSearchExpressionParser;

    @Autowired
    SurveyFactory surveyFactory;

    @Autowired
    SurveyResponseFactory surveyResponseFactory;


    @Test
    @Transactional
    public void testExecuteBasicSearch(TestInfo info) {
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(getTestName(info));
        StudyEnvironment otherEnv = studyEnvironmentFactory.buildPersisted(getTestName(info));


        Survey survey = surveyFactory.buildPersisted(getTestName(info));

        EnrolleeSearchExpression exp = enrolleeSearchExpressionParser.parseRule(
                "{profile.givenName} = 'Jonas' and {profile.familyName} = 'Salk' and {answer.%s.test_question} = 'answer'".formatted(survey.getStableId())
        );
        surveyFactory.attachToEnv(survey, studyEnv.getId(), true);

        Query query = exp.generateQuery(studyEnv.getId());

//        Assertions.assertEquals(
//                "select enrollee.*, profile.given_name, profile.family_name from enrollee enrollee " +
//                        "left outer join profile profile on (enrollee.profile_id = profile.id) " +
//                        "where ((profile.family_name = ?) and (profile.given_name = ?) " +
//                        "and (enrollee.study_environment_id = ?))",
//                query.getSQL());

        Assertions.assertEquals(0, enrolleeSearchExpressionDao.executeSearch(exp, studyEnv.getId()).size());

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


        SurveyResponse surveyResponse = surveyResponseFactory.buildWithAnswers(
                salk1,
                survey,
                Map.of("test_question", "answer")
        );

        List<EnrolleeSearchResult> results = enrolleeSearchExpressionDao.executeSearch(exp, studyEnv.getId());

        System.out.println(results);
        System.out.println(results.get(0).getEnrollee());

    }

}