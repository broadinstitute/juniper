package bio.terra.pearl.core.dao.survey;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.survey.SurveyResponseFactory;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.service.survey.SurveyService;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class AnswerDaoTests extends BaseSpringBootTest {
  @Autowired
  private AnswerDao answerDao;
  @Autowired
  private SurveyResponseFactory surveyResponseFactory;
  @Autowired
  private SurveyService surveyService;

  @Test
  @Transactional
  public void testEmptyAnswerSaves() {
    SurveyResponse response = surveyResponseFactory.buildPersisted("testAnswerValuesSave");
    Answer stringAnswer = Answer.builder()
        .enrolleeId(response.getEnrolleeId())
        .questionStableId("whatevs")
        .surveyResponseId(response.getId())
        .creatingParticipantUserId(response.getCreatingParticipantUserId())
        .surveyStableId("something")
        .surveyVersion(1)
        .build();
    Answer savedAnswer = answerDao.create(stringAnswer);
    DaoTestUtils.assertGeneratedProperties(savedAnswer);
    assertThat(savedAnswer, samePropertyValuesAs(stringAnswer, "id", "createdAt", "lastUpdatedAt",
        "valueAndType"));
  }

  @Test
  @Transactional
  public void testAnswerValuesSave() {
    SurveyResponse response = surveyResponseFactory.buildPersisted("testAnswerValuesSave");
    Answer stringAnswer = answerForResponse(response, "q1")
        .stringValue("test1234")
        .build();
    Answer savedAnswer = answerDao.create(stringAnswer);
    assertThat(savedAnswer.getStringValue(), equalTo(stringAnswer.getStringValue()));

    Answer objectAnswer = answerForResponse(response, "q2")
        .objectValue("[\"foo\", \"bar\"]")
        .build();
    savedAnswer = answerDao.create(objectAnswer);
    assertThat(savedAnswer.getObjectValue(), equalTo(objectAnswer.getObjectValue()));

    Answer booleanAnswer = answerForResponse(response, "q3")
        .booleanValue(true)
        .build();
    savedAnswer = answerDao.create(booleanAnswer);
    assertThat(savedAnswer.getBooleanValue(), equalTo(booleanAnswer.getBooleanValue()));

    Answer numberAnswer = answerForResponse(response, "q4")
        .numberValue(45.6)
        .build();
    savedAnswer = answerDao.create(numberAnswer);
    assertThat(savedAnswer.getNumberValue(), equalTo(numberAnswer.getNumberValue()));
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
