package bio.terra.pearl.core.dao.participant;


import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.kit.KitRequestFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.ParticipantTaskFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.factory.survey.SurveyResponseFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeSearchResult;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.search.facets.*;
import bio.terra.pearl.core.service.participant.search.facets.sql.*;
import bio.terra.pearl.core.service.survey.AnswerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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
  private KitRequestFactory kitRequestFactory;
  @Autowired
  private SurveyFactory surveyFactory;
  @Autowired
  private SurveyResponseFactory surveyResponseFactory;
  @Autowired
  private ParticipantUserService participantUserService;

  @Test
  @Transactional
  public void testEmptySearch(TestInfo info) {
    StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(getTestName(info));
      Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info), studyEnv);
    StudyEnvironment studyEnv2 = studyEnvironmentFactory.buildPersisted(getTestName(info));
    enrolleeFactory.buildPersisted(getTestName(info), studyEnv2);
    ParticipantUser participantUser = participantUserService.find(enrollee.getParticipantUserId()).get();
    participantUser.setLastLogin(Instant.now());
    participantUserService.update(participantUser);

      List<EnrolleeSearchResult> result = enrolleeSearchDao.search(studyEnv.getId(), List.of());
    assertThat(result, hasSize(1));
    assertThat(result.get(0).getEnrollee().getShortcode(), equalTo(enrollee.getShortcode()));

    assertThat(result.get(0).getParticipantUser().getUsername(), equalTo(participantUser.getUsername()));
    assertThat(result.get(0).getParticipantUser().getLastLogin(), greaterThan(Instant.now().minusMillis(3000)));
  }

  @Test
  @Transactional
  public void testKitRequestStatusReturn(TestInfo info) throws Exception {
    StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(getTestName(info));
      Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info), studyEnv);
      Enrollee kitEnrollee = enrolleeFactory.buildPersisted(getTestName(info), studyEnv);

    kitRequestFactory.buildPersisted(getTestName(info), kitEnrollee);

      List<EnrolleeSearchResult> result = enrolleeSearchDao.search(studyEnv.getId(), List.of());
    assertThat(result, hasSize(2));
      EnrolleeSearchResult kitEnrolleeResult = result.stream().filter(esr -> esr.getEnrollee().getShortcode().equals(kitEnrollee.getShortcode()))
            .findFirst().get();
    assertThat(kitEnrolleeResult.getMostRecentKitStatus(), equalTo(KitRequestStatus.CREATED));
      EnrolleeSearchResult otherEnrolleeResult = result.stream().filter(esr -> esr.getEnrollee().getShortcode().equals(enrollee.getShortcode()))
        .findFirst().get();
    assertThat(otherEnrolleeResult.getMostRecentKitStatus(), equalTo(null));
  }

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

  @Test
  @Transactional
  public void testProfileAgeSearch(TestInfo info) {
    StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(getTestName(info));
    Profile profile = Profile.builder().birthDate(LocalDate.of(2011, 1, 1)).build();
    Enrollee youngEnrollee = enrolleeFactory.buildPersisted(getTestName(info), studyEnv, profile);
    Profile profile2 = Profile.builder().birthDate(LocalDate.of(1940, 1, 1)).build();
    Enrollee oldEnrollee = enrolleeFactory.buildPersisted(getTestName(info), studyEnv, profile2);

    SqlSearchableFacet facet = new SqlSearchableFacet(new IntRangeFacetValue(
        "age", 0, 40), new ProfileAgeFacetSqlGenerator());
      List<EnrolleeSearchResult> result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
    assertThat(result, hasSize(1));
    assertThat(result.get(0).getEnrollee().getShortcode(), equalTo(youngEnrollee.getShortcode()));

    facet = new SqlSearchableFacet(new IntRangeFacetValue(
        "age", 50, null), new ProfileAgeFacetSqlGenerator());
    result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
    assertThat(result, hasSize(1));
    assertThat(result.get(0).getEnrollee().getShortcode(), equalTo(oldEnrollee.getShortcode()));

    facet = new SqlSearchableFacet(new IntRangeFacetValue(
        "age", null, null), new ProfileAgeFacetSqlGenerator());
    result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
    assertThat(result, hasSize(2));
  }

  @Test
  @Transactional
  public void testTaskSearch(TestInfo info) throws Exception {
    PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info), EnvironmentName.live);
    StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));
    // enrollee who has completed both surveys
      EnrolleeFactory.EnrolleeBundle doneEnrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv);
    participantTaskFactory.buildPersisted(doneEnrolleeBundle, "bigSurvey", TaskStatus.COMPLETE, TaskType.SURVEY);
    participantTaskFactory.buildPersisted(doneEnrolleeBundle, "otherSurvey", TaskStatus.COMPLETE, TaskType.SURVEY);

    // enrollee who has only  one survey in progress
      EnrolleeFactory.EnrolleeBundle inProgressEnrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv);
    participantTaskFactory.buildPersisted(inProgressEnrolleeBundle, "bigSurvey", TaskStatus.IN_PROGRESS, TaskType.SURVEY);

    // enrollee with no tasks
      EnrolleeFactory.EnrolleeBundle untaskedEnrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv);

    // enrollee who has only completed the big survey
      EnrolleeFactory.EnrolleeBundle oneSurveyEnrollee = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv);
    participantTaskFactory.buildPersisted(oneSurveyEnrollee, "bigSurvey", TaskStatus.COMPLETE, TaskType.SURVEY);

    SqlSearchableFacet facet = new SqlSearchableFacet(new CombinedStableIdFacetValue("status",
        List.of(new StableIdStringFacetValue("status", "bigSurvey", List.of("COMPLETE")))), new ParticipantTaskFacetSqlGenerator());
      List<EnrolleeSearchResult> result = enrolleeSearchDao.search(studyEnv.getId(), List.of(facet));
    assertThat(result, hasSize(2));
    assertThat(result.stream().map(resultMap -> resultMap.getEnrollee().getShortcode()).toList(),
        hasItems(doneEnrolleeBundle.enrollee().getShortcode(),oneSurveyEnrollee.enrollee().getShortcode() ));

    SqlSearchableFacet otherFacet = new SqlSearchableFacet(new CombinedStableIdFacetValue("status",
        List.of(new StableIdStringFacetValue("status", "otherSurvey", List.of("COMPLETE")))), new ParticipantTaskFacetSqlGenerator());

      List<EnrolleeSearchResult> otherResult = enrolleeSearchDao.search(studyEnv.getId(), List.of(otherFacet));
    assertThat(otherResult, hasSize(1));
    assertThat(otherResult.get(0).getEnrollee().getShortcode(), equalTo(doneEnrolleeBundle.enrollee().getShortcode()));


    SqlSearchableFacet bothSurveyFacet = new SqlSearchableFacet(new CombinedStableIdFacetValue("status",
        List.of(new StableIdStringFacetValue("status", "bigSurvey", List.of("COMPLETE")),
            new StableIdStringFacetValue("status", "otherSurvey", List.of("COMPLETE")))), new ParticipantTaskFacetSqlGenerator());
      List<EnrolleeSearchResult> bothSurveyResult = enrolleeSearchDao.search(studyEnv.getId(), List.of(bothSurveyFacet));
    assertThat(bothSurveyResult, hasSize(1));
    assertThat(bothSurveyResult.get(0).getEnrollee().getShortcode(), equalTo(doneEnrolleeBundle.enrollee().getShortcode()));
  }

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

  @Autowired
  private AnswerService answerService;
}
