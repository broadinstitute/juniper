package bio.terra.pearl.core.dao.survey;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.StudyEnvironmentBundle;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.factory.survey.SurveyResponseFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.AnswerFormat;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.service.survey.SurveyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.samePropertyValuesAs;

public class AnswerDaoTests extends BaseSpringBootTest {
  @Autowired
  private AnswerDao answerDao;
  @Autowired
  private SurveyResponseFactory surveyResponseFactory;
  @Autowired
  private SurveyService surveyService;
  @Autowired
  private SurveyFactory surveyFactory;
  @Autowired
  private StudyEnvironmentFactory studyEnvironmentFactory;
  @Autowired
  private EnrolleeFactory enrolleeFactory;

  @Test
  @Transactional
  public void testEmptyAnswerSaves(TestInfo info) {
    SurveyResponse response = surveyResponseFactory.buildPersisted(getTestName(info));
    Answer stringAnswer = Answer.builder()
        .enrolleeId(response.getEnrolleeId())
        .questionStableId("whatevs")
        .surveyResponseId(response.getId())
        .creatingParticipantUserId(response.getCreatingParticipantUserId())
        .surveyStableId("something")
        .surveyVersion(1)
            .format(AnswerFormat.NONE)
        .build();
    Answer savedAnswer = answerDao.create(stringAnswer);
    DaoTestUtils.assertGeneratedProperties(savedAnswer);
    assertThat(savedAnswer, samePropertyValuesAs(stringAnswer, "id", "createdAt", "lastUpdatedAt", "valueAndType", "creatingEntity"));
  }

  @Test
  @Transactional
  public void testAnswerValuesSave(TestInfo info) {
    SurveyResponse response = surveyResponseFactory.buildPersisted(getTestName(info));
    Answer stringAnswer = answerForResponse(response, "q1")
        .stringValue("test1234")
            .format(AnswerFormat.NONE)
        .build();
    Answer savedAnswer = answerDao.create(stringAnswer);
    assertThat(savedAnswer.getStringValue(), equalTo(stringAnswer.getStringValue()));

    Answer objectAnswer = answerForResponse(response, "q2")
        .objectValue("[\"foo\", \"bar\"]")
            .format(AnswerFormat.NONE)
        .build();
    savedAnswer = answerDao.create(objectAnswer);
    assertThat(savedAnswer.getObjectValue(), equalTo(objectAnswer.getObjectValue()));

    Answer booleanAnswer = answerForResponse(response, "q3")
        .booleanValue(true)
            .format(AnswerFormat.NONE)
        .build();
    savedAnswer = answerDao.create(booleanAnswer);
    assertThat(savedAnswer.getBooleanValue(), equalTo(booleanAnswer.getBooleanValue()));

    Answer numberAnswer = answerForResponse(response, "q4")
        .numberValue(45.6)
            .format(AnswerFormat.NONE)
        .build();
    savedAnswer = answerDao.create(numberAnswer);
    assertThat(savedAnswer.getNumberValue(), equalTo(numberAnswer.getNumberValue()));
  }

  @Test
  @Transactional
  void testFindByProfileIdStudyAndQuestion(TestInfo info) {
    StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
    PortalEnvironment portalEnv = studyEnvBundle.getPortalEnv();
    StudyEnvironment studyEnv = studyEnvBundle.getStudyEnv();

    StudyEnvironmentBundle studyEnvBundle2 = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox, studyEnvBundle.getPortal(), studyEnvBundle.getPortalEnv());
    StudyEnvironment studyEnv2 = studyEnvBundle2.getStudyEnv();

    Survey surveyInEnv1 = surveyFactory.buildPersisted(getTestName(info), portalEnv.getPortalId());

    surveyFactory.attachToEnv(surveyInEnv1, studyEnv.getId(), true);

    Survey surveyInEnv2 = surveyFactory.buildPersisted(getTestName(info), portalEnv.getPortalId());

    surveyFactory.attachToEnv(surveyInEnv2, studyEnv2.getId(), true);

    EnrolleeBundle bundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv);

    Enrollee enrollee1 = bundle.enrollee();
    Enrollee enrollee2 = enrolleeFactory.buildPersisted(getTestName(info), studyEnv2.getId(), enrollee1.getParticipantUserId(), enrollee1.getProfileId());
    EnrolleeBundle unrelatedEnrollee = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv);


    surveyResponseFactory.buildWithAnswers(enrollee1, surveyInEnv1, Map.of("question_env_1", "answer1"));
    surveyResponseFactory.buildWithAnswers(enrollee2, surveyInEnv2, Map.of("question_env_2", "differentAnswer"));
    surveyResponseFactory.buildWithAnswers(unrelatedEnrollee.enrollee(), surveyInEnv2, Map.of("different_question", "something"));

    // find question that enrollee 1 filled out
    Optional<Answer> answer = answerDao.findByProfileIdStudyAndQuestion(
            enrollee1.getProfileId(),
            studyEnvBundle.getStudy().getShortcode(),
            surveyInEnv1.getStableId(),
            "question_env_1");
    assertThat(answer.get().getStringValue(), equalTo("answer1"));

    // find question that enrollee 2 filled out
    answer = answerDao.findByProfileIdStudyAndQuestion(
            enrollee1.getProfileId(),
            studyEnvBundle2.getStudy().getShortcode(),
            surveyInEnv2.getStableId(),
            "question_env_2");
    assertThat(answer.get().getStringValue(), equalTo("differentAnswer"));


    // find question that enrollee 2 filled out but ask for wrong study
    answer = answerDao.findByProfileIdStudyAndQuestion(
            enrollee1.getProfileId(),
            studyEnvBundle.getStudy().getShortcode(),
            surveyInEnv2.getStableId(),
            "question_env_2");
    assertThat(answer.isEmpty(), equalTo(true));

    // find question that neither enrollee 1 nor 2 filled out
    answer = answerDao.findByProfileIdStudyAndQuestion(
            enrollee1.getProfileId(),
            studyEnvBundle2.getStudy().getShortcode(),
            surveyInEnv2.getStableId(),
            "different_question");
    assertThat(answer.isEmpty(), equalTo(true));
  }


  private Answer.AnswerBuilder answerForResponse(SurveyResponse response, String questionStableId) {
    Survey survey = surveyService.find(response.getSurveyId()).get();
    return Answer.builder()
        .enrolleeId(response.getEnrolleeId())
        .questionStableId(questionStableId)
        .surveyResponseId(response.getId())
        .creatingParticipantUserId(response.getCreatingParticipantUserId())
        .surveyStableId(survey.getStableId())
        .surveyVersion(survey.getVersion());
  }
}
