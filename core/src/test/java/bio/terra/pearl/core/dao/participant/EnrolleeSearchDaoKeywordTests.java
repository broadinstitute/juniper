package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.kit.KitRequestFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.ParticipantTaskFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeSearchResult;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.search.facets.StringFacetValue;
import bio.terra.pearl.core.service.participant.search.facets.sql.KeywordFacetSqlGenerator;
import bio.terra.pearl.core.service.participant.search.facets.sql.SqlSearchableFacet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class EnrolleeSearchDaoKeywordTests extends BaseSpringBootTest {
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    private EnrolleeSearchDao enrolleeSearchDao;

    @Test
    @Transactional
    public void testKeywordSearchGivenFamilyName(TestInfo info) {
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(getTestName(info));

        Profile profile = Profile.builder().givenName("mark").familyName("stewart").build();
        Enrollee markGivenNameEnrollee = enrolleeFactory.buildPersisted(getTestName(info), studyEnv, profile);
        Profile profile2 = Profile.builder().givenName("matt").familyName("stover").build();
        Enrollee mattGivenNameEnrollee = enrolleeFactory.buildPersisted(getTestName(info), studyEnv, profile2);
        Profile profile3 = Profile.builder().givenName("steve").familyName("mallory").build();
        Enrollee steveGivenNameEnrollee = enrolleeFactory.buildPersisted(getTestName(info), studyEnv, profile3);

        SqlSearchableFacet facet = new SqlSearchableFacet(new StringFacetValue(
                "keyword", List.of("mark")), new KeywordFacetSqlGenerator());
        List<EnrolleeSearchResult> result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getEnrollee().getShortcode(), equalTo(markGivenNameEnrollee.getShortcode()));

        facet = new SqlSearchableFacet(new StringFacetValue(
                "keyword", List.of("ma")), new KeywordFacetSqlGenerator());
        result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
        assertThat(result, hasSize(3));

        facet = new SqlSearchableFacet(new StringFacetValue(
                "keyword", List.of("allo")), new KeywordFacetSqlGenerator());
        result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getEnrollee().getShortcode(), equalTo(steveGivenNameEnrollee.getShortcode()));
    }

    @Test
    @Transactional
    public void testKeywordSearchEmailShortcode(TestInfo info) {
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(getTestName(info));

        Profile profile = Profile.builder().contactEmail("m@a.com").build();
        Enrollee maEmail = enrolleeFactory.buildPersisted(getTestName(info), studyEnv, profile);
        Profile profile2 = Profile.builder().contactEmail("foo@a.com").familyName("stover").build();
        Enrollee fooEmail = enrolleeFactory.buildPersisted(getTestName(info), studyEnv, profile2);

        SqlSearchableFacet facet = new SqlSearchableFacet(new StringFacetValue(
                "keyword", List.of(maEmail.getShortcode())), new KeywordFacetSqlGenerator());
        List<EnrolleeSearchResult> result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getEnrollee().getShortcode(), equalTo(maEmail.getShortcode()));

        facet = new SqlSearchableFacet(new StringFacetValue(
                "keyword", List.of("a.com")), new KeywordFacetSqlGenerator());
        result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
        assertThat(result, hasSize(2));
    }

}
