package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.SurveyQuestionDefinition;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SurveyFormatter {

    public static Map<String, String> formatAllResponses(List<SurveyResponse> responses,
                                                       List<SurveyQuestionDefinition> definitions,
                                                       List<Answer> answers) {
        Map<String, SurveyQuestionDefinition> questionsByStableId = new HashMap<>();
        for (SurveyQuestionDefinition questionDef : definitions) {
            questionsByStableId.put(questionDef.getQuestionStableId(), questionDef);
        }

    }


}
