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
import bio.terra.pearl.core.service.participant.search.facets.AgeRangeFacetValue;
import bio.terra.pearl.core.service.participant.search.facets.StringFacetValue;
import bio.terra.pearl.core.service.participant.search.facets.sql.ProfileFacetSqlGenerator;
import bio.terra.pearl.core.service.participant.search.facets.sql.SqlSearchableFacet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class EnrolleeSearchDaoProfileTests extends BaseSpringBootTest {
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    private EnrolleeSearchDao enrolleeSearchDao;

    @Test
    @Transactional
    public void testProfileSearch(TestInfo info) {
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(getTestName(info));
        Profile profile = Profile.builder().sexAtBirth("male").build();
        Enrollee maleEnrollee = enrolleeFactory.buildPersisted(getTestName(info), studyEnv, profile);
        Profile profile2 = Profile.builder().sexAtBirth("female").build();
        enrolleeFactory.buildPersisted(getTestName(info), studyEnv, profile2);

        SqlSearchableFacet facet = new SqlSearchableFacet(new StringFacetValue(
                "sexAtBirth", List.of("male")), new ProfileFacetSqlGenerator());
        List<EnrolleeSearchResult> result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getEnrollee().getShortcode(), equalTo(maleEnrollee.getShortcode()));
        assertThat(result.get(0).getProfile().getSexAtBirth(), equalTo("male"));
    }


    @Test
    @Transactional
    public void testProfileAgeSearch(TestInfo info) {
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(getTestName(info));
        Profile profile = Profile.builder().birthDate(LocalDate.of(2011, 1, 1)).build();
        Enrollee youngEnrollee = enrolleeFactory.buildPersisted(getTestName(info), studyEnv, profile);
        Profile profile2 = Profile.builder().birthDate(LocalDate.of(1940, 1, 1)).build();
        Enrollee oldEnrollee = enrolleeFactory.buildPersisted(getTestName(info), studyEnv, profile2);

        SqlSearchableFacet facet = new SqlSearchableFacet(new AgeRangeFacetValue(
                "age", 0, 40), new ProfileFacetSqlGenerator());
        List<EnrolleeSearchResult> result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getEnrollee().getShortcode(), equalTo(youngEnrollee.getShortcode()));

        facet = new SqlSearchableFacet(new AgeRangeFacetValue(
                "age", 50, null), new ProfileFacetSqlGenerator());
        result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getEnrollee().getShortcode(), equalTo(oldEnrollee.getShortcode()));

        facet = new SqlSearchableFacet(new AgeRangeFacetValue(
                "age", null, null), new ProfileFacetSqlGenerator());
        result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
        assertThat(result, hasSize(2));
    }
}
