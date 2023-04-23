package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.model.survey.QuestionChoice;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyQuestionDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SurveyParseUtils {
    public static final String SURVEY_JS_CHECKBOX_TYPE = "checkbox";
    public static final String SURVEY_JS_SHOW_OTHER = "showOtherItem";
    public static final String SURVEY_JS_OTHER_TEXT_PROP = "otherText";
    public static final String SURVEY_JS_OTHER_VALUE = "other";
    public static final String SURVEY_JS_SHOW_NONE = "showNoneItem";
    public static final String SURVEY_JS_NONE_VALUE_PROP = "noneValue";
    public static final String SURVEY_JS_NONE_TEXT_PROP = "noneText";

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

    public static SurveyQuestionDefinition unmarshalSurveyQuestion(Survey survey, JsonNode question,
                                                                   Map<String, JsonNode> questionTemplates, int globalOrder) {

        SurveyQuestionDefinition definition = SurveyQuestionDefinition.builder()
                .surveyId(survey.getId())
                .surveyStableId(survey.getStableId())
                .surveyVersion(survey.getVersion())
                .questionStableId(question.get("name").asText())
                .exportOrder(globalOrder)
                .build();

        //The following fields may either be specified in the question itself,
        //or as part of a question template. Resolve the remaining fields against
        //the template (if applicable), so we have the full question definition.
        JsonNode templatedQuestion = question.has("questionTemplateName") ?
                questionTemplates.get(question.get("questionTemplateName").asText()) :
                question;

        definition.setQuestionType(templatedQuestion.get("type").asText());
        if (definition.getQuestionType().equals(SURVEY_JS_CHECKBOX_TYPE)) {
            definition.setAllowMultiple(true);
        }
        if (templatedQuestion.has(SURVEY_JS_SHOW_OTHER)) {
            definition.setAllowOtherDescription(templatedQuestion.get(SURVEY_JS_SHOW_OTHER).asBoolean());
        }

        //For normal elements, we'll store the title in the question_text column
        //For HTML elements which don't have a title, we'll store the HTML instead
        if(templatedQuestion.has("title")) {
            definition.setQuestionText(templatedQuestion.get("title").asText());
        } else if(templatedQuestion.has("html")) {
            definition.setQuestionText(templatedQuestion.get("html").asText());
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
        List<QuestionChoice> choices = new ArrayList<>();
        for(JsonNode choice : question.get("choices")) {
            // if all text/value pairs are the same, surveyjs transforms the choices into an array of strings.  grrrr...
            if (choice.isTextual()) {
                choices.add(new QuestionChoice(choice.asText(), choice.asText()));
            } else {
                choices.add(new QuestionChoice(choice.get("value").asText(), choice.get("text").asText()));
            }
        }
        /**
         * Add choices to represent the none/others if they exist. Mapping them as actual choices allows for simpler
         * downstream logic
         */
        if (question.has(SURVEY_JS_SHOW_OTHER)) {
            String otherText = question.has(SURVEY_JS_OTHER_TEXT_PROP) ? question.get(SURVEY_JS_OTHER_TEXT_PROP).asText() : "other";
            choices.add(new QuestionChoice(SURVEY_JS_OTHER_VALUE, otherText));
        }
        if (question.has(SURVEY_JS_SHOW_NONE)) {
            String noneText = question.has(SURVEY_JS_NONE_TEXT_PROP) ? question.get(SURVEY_JS_NONE_TEXT_PROP).asText() : "None";
            String noneValue = question.has(SURVEY_JS_NONE_VALUE_PROP) ? question.get(SURVEY_JS_NONE_VALUE_PROP).asText() : "none";
            choices.add(new QuestionChoice(noneValue, noneText));
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
