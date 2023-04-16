package bio.terra.pearl.core.factory.survey;

import bio.terra.pearl.core.model.survey.Answer;
import java.util.List;
import java.util.Map;

public class AnswerFactory {
    public static List<Answer> fromMap(Map<String, Object> valueMap) {
       return valueMap.entrySet().stream().map(entry -> {
            Answer answer = Answer.builder()
                    .questionStableId(entry.getKey())
                    .build();
            answer.setValue(entry.getValue());
            return answer;
        }).toList();
    }
}
