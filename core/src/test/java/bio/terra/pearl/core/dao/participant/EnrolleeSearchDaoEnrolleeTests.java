package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeSearchResult;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.participant.search.facets.BooleanFacetValue;
import bio.terra.pearl.core.service.participant.search.facets.sql.EnrolleeFacetSqlGenerator;
import bio.terra.pearl.core.service.participant.search.facets.sql.SqlSearchableFacet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class EnrolleeSearchDaoEnrolleeTests extends BaseSpringBootTest {
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    private EnrolleeSearchDao enrolleeSearchDao;

    @Test
    @Transactional
    public void testConsentedSearch(TestInfo info) {
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(getTestName(info));
        Enrollee consentedEnrollee =  enrolleeFactory.buildPersisted(enrolleeFactory.builderWithDependencies(getTestName(info), studyEnv).consented(true));
        Enrollee unconsentedEnrollee =  enrolleeFactory.buildPersisted(enrolleeFactory.builderWithDependencies(getTestName(info), studyEnv).consented(false));

        SqlSearchableFacet facet = new SqlSearchableFacet(new BooleanFacetValue(
                "consented", true), new EnrolleeFacetSqlGenerator());
        List<EnrolleeSearchResult> result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getEnrollee().getShortcode(), equalTo(consentedEnrollee.getShortcode()));

        facet = new SqlSearchableFacet(new BooleanFacetValue(
                "consented", false), new EnrolleeFacetSqlGenerator());
        result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getEnrollee().getShortcode(), equalTo(unconsentedEnrollee.getShortcode()));

        facet = new SqlSearchableFacet(new BooleanFacetValue(
                "consented", null), new EnrolleeFacetSqlGenerator());
        result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
        assertThat(result, hasSize(2));
    }

    @Test
    @Transactional
    public void testSubjectConsentedSearch(TestInfo info) {
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(getTestName(info));
        Enrollee consentedEnrollee =  enrolleeFactory.buildPersisted(enrolleeFactory.builderWithDependencies(getTestName(info), studyEnv).consented(true));
        Enrollee unconsentedEnrollee =  enrolleeFactory.buildPersisted(enrolleeFactory.builderWithDependencies(getTestName(info), studyEnv).consented(false));
        Enrollee consentedProxy =  enrolleeFactory.buildPersisted(enrolleeFactory.builderWithDependencies(getTestName(info), studyEnv).subject(false).consented(true));

        // find both subjects
        SqlSearchableFacet facet = new SqlSearchableFacet(new BooleanFacetValue(
                "subject", true), new EnrolleeFacetSqlGenerator());
        List<EnrolleeSearchResult> result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
        assertThat(result, hasSize(2));
        assertThat(result.stream().map(EnrolleeSearchResult::getEnrollee).toList(), containsInAnyOrder(consentedEnrollee, unconsentedEnrollee));

        // find just the consented subject
        facet = new SqlSearchableFacet(new BooleanFacetValue(
                "subject", true), new EnrolleeFacetSqlGenerator());
        List<SqlSearchableFacet> facets = List.of(facet, new SqlSearchableFacet(new BooleanFacetValue(
                "consented", true), new EnrolleeFacetSqlGenerator()));
        result = enrolleeSearchDao.search(studyEnv.getId(), facets);
        assertThat(result, hasSize(1));
        assertThat(result.stream().map(EnrolleeSearchResult::getEnrollee).toList(), contains(consentedEnrollee));

        facet = new SqlSearchableFacet(new BooleanFacetValue(
                "subject", false), new EnrolleeFacetSqlGenerator());
        result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getEnrollee().getShortcode(), equalTo(consentedProxy.getShortcode()));
    }

}
