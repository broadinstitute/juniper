package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyQuestionDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SurveyParseUtils {
/** recursively gets all questions from the given node */
    public static List<JsonNode> getAllQuestions(JsonNode containerElement) {
        List<JsonNode> elements = new ArrayList<>();

        if(containerElement.has("elements")) {
            for(JsonNode element : containerElement.get("elements")) {
                elements.addAll(getAllQuestions(element));
            }
        } else elements.add(containerElement);

        return elements;
    }

    public static SurveyQuestionDefinition unmarshalSurveyQuestion(Survey survey, JsonNode question, Map<String, JsonNode> questionTemplates) {

        SurveyQuestionDefinition definition = SurveyQuestionDefinition.builder()
                .surveyId(survey.getId())
                .surveyStableId(survey.getStableId())
                .surveyVersion(survey.getVersion())
                .questionStableId(question.get("name").asText())
                .build();

        //The following fields may either be specified in the question itself,
        //or as part of a question template. Resolve the remaining fields against
        //the template (if applicable), so we have the full question definition.
        JsonNode templatedQuestion = question.has("questionTemplateName") ?
                questionTemplates.get(question.get("questionTemplateName").asText()) :
                question;

        definition.setQuestionType(templatedQuestion.get("type").asText());

        if(templatedQuestion.has("title")) {
            definition.setQuestionText(templatedQuestion.get("title").asText());
        }

        if(templatedQuestion.has("isRequired")){
            definition.setRequired(templatedQuestion.get("isRequired").asBoolean());
        }

        if(templatedQuestion.has("choices")){
            definition.setChoices(unmarshalSurveyQuestionChoices(templatedQuestion));
        }

        return definition;
    }

    public static String unmarshalSurveyQuestionChoices(JsonNode question) {
        Map<String, String> choices = new HashMap<>();
        for(JsonNode choice : question.get("choices")){
            choices.put(choice.get("value").asText(), choice.get("text").asText());
        }
        ObjectMapper mapper = new ObjectMapper();

        String result;
        try {
            result = mapper.writeValueAsString(choices);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Question contains malformed choices");
        }

        return result;
    }

}
