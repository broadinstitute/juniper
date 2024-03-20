package bio.terra.pearl.core.dao.search;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpression;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import org.jooq.Query;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class EnrolleeSearchExpressionDaoTests extends BaseSpringBootTest {

    @Autowired
    EnrolleeSearchExpressionDao enrolleeSearchExpressionDao;

    @Autowired
    EnrolleeFactory enrolleeFactory;

    @Autowired
    StudyEnvironmentFactory studyEnvironmentFactory;

    @Autowired
    EnrolleeSearchExpressionParser enrolleeSearchExpressionParser;


    @Test
    @Transactional
    public void testExecuteBasicSearch(TestInfo info) {
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(getTestName(info));
        StudyEnvironment otherEnv = studyEnvironmentFactory.buildPersisted(getTestName(info));

        EnrolleeSearchExpression exp = enrolleeSearchExpressionParser.parseRule("{profile.givenName} = 'Jonas' and {profile.familyName} = 'Salk'");

        Query query = exp.generateQuery(studyEnv.getId());

        Assertions.assertEquals(
                "select enrollee.*, profile.given_name, profile.family_name from enrollee enrollee " +
                        "left outer join profile profile on (enrollee.profile_id = profile.id) " +
                        "where ((profile.family_name = ?) and (profile.given_name = ?) " +
                        "and (enrollee.study_environment_id = ?))",
                query.getSQL());

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


        List<Enrollee> results = enrolleeSearchExpressionDao.executeSearch(exp, studyEnv.getId());

        Assertions.assertEquals(2, results.size());
        Assertions.assertTrue(results.contains(salk1));
        Assertions.assertTrue(results.contains(salk2));
        Assertions.assertFalse(results.contains(salk3NotInEnv));
        Assertions.assertFalse(results.contains(notSalk));

    }

}