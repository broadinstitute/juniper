package bio.terra.pearl.core.factory.survey;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.service.survey.AnswerService;
import bio.terra.pearl.core.service.survey.SurveyService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class AnswerFactory {
    @Autowired
    private AnswerService answerService;

    public static List<Answer> fromMap(Map<String, Object> valueMap) {
       return valueMap.entrySet().stream().map(entry -> {
            Answer answer = Answer.builder()
                    .questionStableId(entry.getKey())
                    .build();
            answer.setValueAndType(entry.getValue());
            return answer;
        }).toList();
    }

    public List<Answer> createFromMap(Map<String, Object> valueMap,
                                       Enrollee enrollee,
                                       Survey survey,
                                       SurveyResponse surveyResponse) {
        List<Answer> answers = valueMap.entrySet().stream().map(entry -> {
            Answer answer = Answer.builder()
                    .questionStableId(entry.getKey())
                    .creatingParticipantUserId(enrollee.getParticipantUserId())
                    .enrolleeId(enrollee.getId())
                    .surveyResponseId(surveyResponse.getId())
                    .surveyStableId(survey.getStableId())
                    .surveyVersion(survey.getVersion())
                    .build();
            if (entry.getValue() instanceof JsonNode) {
                answer.setObjectValue(entry.getValue().toString());
            } else {
                answer.setValueAndType(entry.getValue());
            }
            answer = answerService.create(answer);
            return answer;
        }).toList();
        return answers;
    }
}
