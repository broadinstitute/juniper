package bio.terra.pearl.core.dao.participant;


import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.ParticipantTaskFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.participant.search.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.Test;
import org.postgresql.jdbc.PgArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class EnrolleeSearchDaoTests extends BaseSpringBootTest {
  @Autowired
  private EnrolleeFactory enrolleeFactory;
  @Autowired
  private StudyEnvironmentFactory studyEnvironmentFactory;
  @Autowired
  private PortalEnvironmentFactory portalEnvironmentFactory;
  @Autowired
  private ParticipantTaskFactory participantTaskFactory;
  @Autowired
  private EnrolleeSearchDao enrolleeSearchDao;
  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @Transactional
  public void testEmptySearch() {
    StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted("testEmptySearch");
    enrolleeFactory.buildPersisted("testEmptySearch", studyEnv);
    StudyEnvironment studyEnv2 = studyEnvironmentFactory.buildPersisted("testEmptySearch");
    enrolleeFactory.buildPersisted("testEmptySearch", studyEnv2);

    var result = enrolleeSearchDao.search(studyEnv.getId(), List.of());
    assertThat(result, hasSize(1));
  }

  @Test
  @Transactional
  public void testProfileSearch() {
    StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted("testProfileSearch");
    Profile profile = Profile.builder().sexAtBirth("male").build();
    Enrollee maleEnrollee = enrolleeFactory.buildPersisted("testProfileSearch", studyEnv, profile);
    Profile profile2 = Profile.builder().sexAtBirth("female").build();
    enrolleeFactory.buildPersisted("testProfileSearch", studyEnv, profile2);

    ProfileFacetValue facet = new ProfileFacetValue(new StringFacetValue(
        "sexAtBirth", List.of("male")));
    var result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
    assertThat(result, hasSize(1));
    assertThat(result.get(0).get("enrollee__shortcode"), equalTo(maleEnrollee.getShortcode()));
    assertThat(result.get(0).get("profile__sex_at_birth"), equalTo("male"));
  }

  @Test
  @Transactional
  public void testProfileAgeSearch() {
    StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted("testProfileSearch");
    Profile profile = Profile.builder().birthDate(LocalDate.of(2011, 1, 1)).build();
    Enrollee youngEnrollee = enrolleeFactory.buildPersisted("testProfileSearch", studyEnv, profile);
    Profile profile2 = Profile.builder().birthDate(LocalDate.of(1940, 1, 1)).build();
    Enrollee oldEnrollee = enrolleeFactory.buildPersisted("testProfileSearch", studyEnv, profile2);

    ProfileAgeFacetValue facet = new ProfileAgeFacetValue(new IntRangeFacetValue(
        "age", 0, 40));
    var result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
    assertThat(result, hasSize(1));
    assertThat(result.get(0).get("enrollee__shortcode"), equalTo(youngEnrollee.getShortcode()));

    facet = new ProfileAgeFacetValue(new IntRangeFacetValue(
        "age", 50, null));
    result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
    assertThat(result, hasSize(1));
    assertThat(result.get(0).get("enrollee__shortcode"), equalTo(oldEnrollee.getShortcode()));

    facet = new ProfileAgeFacetValue(new IntRangeFacetValue(
        "age", null, null));
    result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
    assertThat(result, hasSize(2));
  }

  @Test
  @Transactional
  public void testTaskSearch() throws Exception {
    PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted("testTaskSearch", EnvironmentName.live );
    StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, "testTaskSearch");
    var doneEnrolleeBundle = enrolleeFactory.buildWithPortalUser("testTaskSearch", portalEnv, studyEnv);
    participantTaskFactory.buildPersisted(doneEnrolleeBundle, "bigSurvey", TaskStatus.COMPLETE, TaskType.SURVEY);

    var inProgressEnrolleeBundle = enrolleeFactory.buildWithPortalUser("testFindByStatusAndTimeMulti", portalEnv, studyEnv);
    participantTaskFactory.buildPersisted(inProgressEnrolleeBundle, "bigSurvey", TaskStatus.IN_PROGRESS, TaskType.SURVEY);

    var untaskedEnrolleeBundle = enrolleeFactory.buildWithPortalUser("testFindByStatusAndTimeMulti", portalEnv, studyEnv);

    var differentTaskBundle = enrolleeFactory.buildWithPortalUser("testFindByStatusAndTimeMulti", portalEnv, studyEnv);
    participantTaskFactory.buildPersisted(differentTaskBundle, "otherSurvey", TaskStatus.COMPLETE, TaskType.SURVEY);

    ParticipantTaskFacetValue facet = new ParticipantTaskFacetValue(new StableIdFacetValue(
        "status", List.of("COMPLETE"), "bigSurvey"));
    var result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
    assertThat(result, hasSize(1));
    assertThat(result.get(0).get("enrollee__shortcode"), equalTo(doneEnrolleeBundle.enrollee().getShortcode()));

    assertThat(((PgArray) result.get(0).get("participant_task__status")).getArray(), equalTo(List.of("COMPLETE").toArray()));
    assertThat(((PgArray) result.get(0).get("participant_task__target_stable_id")).getArray(), equalTo(List.of("bigSurvey").toArray()));

    ParticipantTaskFacetValue otherFacet = new ParticipantTaskFacetValue(new StableIdFacetValue(
        "status", List.of("COMPLETE"), "otherSurvey"));
    var otherResult = enrolleeSearchDao.search(studyEnv.getId(), List.of(otherFacet));
    assertThat(otherResult, hasSize(1));
    assertThat(otherResult.get(0).get("enrollee__shortcode"), equalTo(differentTaskBundle.enrollee().getShortcode()));
  }



}
