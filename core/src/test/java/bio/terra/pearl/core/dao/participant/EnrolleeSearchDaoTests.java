package bio.terra.pearl.core.dao.participant;


import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.search.EnrolleeSearchExpressionDao;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.kit.KitRequestFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeSearchResult;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpression;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

public class EnrolleeSearchDaoTests extends BaseSpringBootTest {
  @Autowired
  private EnrolleeFactory enrolleeFactory;
  @Autowired
  private StudyEnvironmentFactory studyEnvironmentFactory;
  @Autowired
  private EnrolleeSearchDao enrolleeSearchDao;
  @Autowired
  private KitRequestFactory kitRequestFactory;
  @Autowired
  private ParticipantUserService participantUserService;
  @Autowired
  private EnrolleeSearchExpressionDao enrolleeSearchExpressionDao;
  @Autowired
  private EnrolleeSearchExpressionParser enrolleeSearchExpressionParser;

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

    EnrolleeSearchExpression exp = enrolleeSearchExpressionParser.parseRule("");

    List<bio.terra.pearl.core.model.search.EnrolleeSearchResult> results = enrolleeSearchExpressionDao.executeSearch(exp, studyEnv.getId());
    assertThat(results, hasSize(1));
    assertThat(results.get(0).getEnrollee().getShortcode(), equalTo(enrollee.getShortcode()));
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

}
