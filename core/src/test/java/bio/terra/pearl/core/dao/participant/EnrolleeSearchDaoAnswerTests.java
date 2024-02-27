package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.kit.KitRequestFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.ParticipantTaskFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.factory.survey.SurveyResponseFactory;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeSearchResult;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.search.facets.AnswerFacetValue;
import bio.terra.pearl.core.service.participant.search.facets.sql.AnswerFacetSqlGenerator;
import bio.terra.pearl.core.service.participant.search.facets.sql.SqlSearchableFacet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class EnrolleeSearchDaoAnswerTests extends BaseSpringBootTest {
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    private EnrolleeSearchDao enrolleeSearchDao;
    @Autowired
    private SurveyFactory surveyFactory;
    @Autowired
    private SurveyResponseFactory surveyResponseFactory;

    @Test
    @Transactional
    public void testStringAnswerSearch(TestInfo info) {
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(getTestName(info));
        Enrollee enrollee1 = enrolleeFactory.buildPersisted(getTestName(info), studyEnv);
        Enrollee enrollee2 = enrolleeFactory.buildPersisted(getTestName(info), studyEnv);
        Enrollee enrollee3 = enrolleeFactory.buildPersisted(getTestName(info), studyEnv);

        Survey survey = surveyFactory.buildPersisted(getTestName(info));
        surveyResponseFactory.buildWithAnswers(enrollee1, survey, Map.of("question1", "foo", "question2", "bar"));
        surveyResponseFactory.buildWithAnswers(enrollee2, survey, Map.of("question1", "foo", "question2", "blo"));

        // search should return both enrollees with the same answer
        SqlSearchableFacet facet = new SqlSearchableFacet(
                new AnswerFacetValue(survey.getStableId(), "question1", "foo"), new AnswerFacetSqlGenerator());
        List<EnrolleeSearchResult> result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
        assertThat(result.stream().map(EnrolleeSearchResult::getEnrollee).toList(), containsInAnyOrder(enrollee1, enrollee2));

        // check that a search for a different value returns only the other enrollee
        facet = new SqlSearchableFacet(
                new AnswerFacetValue(survey.getStableId(), "question2", "blo"), new AnswerFacetSqlGenerator());
        result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
        assertThat(result.stream().map(EnrolleeSearchResult::getEnrollee).toList(), contains(enrollee2));

        // check that a search for a nonexistent value returns no results
        facet = new SqlSearchableFacet(
                new AnswerFacetValue(survey.getStableId(), "question1", "zzz"), new AnswerFacetSqlGenerator());
        result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
        assertThat(result.stream().map(EnrolleeSearchResult::getEnrollee).toList(), hasSize(0));
    }

    @Test
    @Transactional
    public void testNumberAnswerSearch(TestInfo info) {
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(getTestName(info));
        Enrollee enrollee1 = enrolleeFactory.buildPersisted(getTestName(info), studyEnv);
        Enrollee enrollee2 = enrolleeFactory.buildPersisted(getTestName(info), studyEnv);
        Enrollee enrollee3 = enrolleeFactory.buildPersisted(getTestName(info), studyEnv);

        Survey survey = surveyFactory.buildPersisted(getTestName(info));
        surveyResponseFactory.buildWithAnswers(enrollee1, survey, Map.of("question1", 1, "question2", 1.5));
        surveyResponseFactory.buildWithAnswers(enrollee2, survey, Map.of("question1", 1, "question2", 1.1));

        // search should return both enrollees with the same answer
        SqlSearchableFacet facet = new SqlSearchableFacet(
                new AnswerFacetValue(survey.getStableId(), "question1", 1D), new AnswerFacetSqlGenerator());
        List<EnrolleeSearchResult> result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
        assertThat(result.stream().map(EnrolleeSearchResult::getEnrollee).toList(), containsInAnyOrder(enrollee1, enrollee2));

        // check that a search for a different value returns only the other enrollee
        facet = new SqlSearchableFacet(
                new AnswerFacetValue(survey.getStableId(), "question2", 1.5), new AnswerFacetSqlGenerator());
        result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
        assertThat(result.stream().map(EnrolleeSearchResult::getEnrollee).toList(), contains(enrollee1));

        // check that a search for a nonexistent value returns no results
        facet = new SqlSearchableFacet(
                new AnswerFacetValue(survey.getStableId(), "question1", 0D), new AnswerFacetSqlGenerator());
        result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
        assertThat(result.stream().map(EnrolleeSearchResult::getEnrollee).toList(), hasSize(0));
    }

    @Test
    @Transactional
    public void testBooleanAnswerSearch(TestInfo info) {
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(getTestName(info));
        Enrollee enrollee1 = enrolleeFactory.buildPersisted(getTestName(info), studyEnv);
        Enrollee enrollee2 = enrolleeFactory.buildPersisted(getTestName(info), studyEnv);
        Enrollee enrollee3 = enrolleeFactory.buildPersisted(getTestName(info), studyEnv);

        Survey survey = surveyFactory.buildPersisted(getTestName(info));
        surveyResponseFactory.buildWithAnswers(enrollee1, survey, Map.of("question1", true, "question2", false));
        surveyResponseFactory.buildWithAnswers(enrollee2, survey, Map.of("question1", true, "question2", true));

        // search should return both enrollees with the same answer
        SqlSearchableFacet facet = new SqlSearchableFacet(
                new AnswerFacetValue(survey.getStableId(), "question1", true), new AnswerFacetSqlGenerator());
        List<EnrolleeSearchResult> result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
        assertThat(result.stream().map(EnrolleeSearchResult::getEnrollee).toList(), containsInAnyOrder(enrollee1, enrollee2));

        // check that a search for a different value returns only the other enrollee
        facet = new SqlSearchableFacet(
                new AnswerFacetValue(survey.getStableId(), "question2", false), new AnswerFacetSqlGenerator());
        result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
        assertThat(result.stream().map(EnrolleeSearchResult::getEnrollee).toList(), contains(enrollee1));

        // check that a search for a nonexistent question returns no results
        facet = new SqlSearchableFacet(
                new AnswerFacetValue(survey.getStableId(), "question3", false), new AnswerFacetSqlGenerator());
        result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
        assertThat(result.stream().map(EnrolleeSearchResult::getEnrollee).toList(), hasSize(0));
    }
}
