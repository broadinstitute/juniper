package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.survey.QuestionChoice;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyQuestionDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.formula.functions.T;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SurveyParseUtils {
    public static final String SURVEY_JS_CHECKBOX_TYPE = "checkbox";
    public static final String SURVEY_JS_SHOW_OTHER = "showOtherItem";
    public static final String SURVEY_JS_OTHER_TEXT_PROP = "otherText";
    public static final String SURVEY_JS_OTHER_VALUE = "other";
    public static final String SURVEY_JS_SHOW_NONE = "showNoneItem";
    public static final String SURVEY_JS_NONE_VALUE_PROP = "noneValue";
    public static final String SURVEY_JS_NONE_TEXT_PROP = "noneText";
    public static final String DERIVED_QUESTION_TYPE = "derived";
    public static final Pattern EXPRESSION_DEPENDENCY = Pattern.compile(".*\\{(.+?)\\}.*");

    /**
     * recursively gets all questions from the given node
     */
    public static List<JsonNode> getAllQuestions(JsonNode containerElement) {
        List<JsonNode> elements = new ArrayList<>();
        if (containerElement.has("elements")) {
            for (JsonNode element : containerElement.get("elements")) {
                elements.addAll(getAllQuestions(element));
            }
        } else {
            elements.add(containerElement);
        }

        return elements;
    }

    public static SurveyQuestionDefinition unmarshalSurveyQuestion(Survey survey, JsonNode question,
                                                                   Map<String, JsonNode> questionTemplates,
                                                                   int globalOrder, boolean isDerived) {

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

        definition.setQuestionType(isDerived ? DERIVED_QUESTION_TYPE : templatedQuestion.get("type").asText());
        if (definition.getQuestionType().equals(SURVEY_JS_CHECKBOX_TYPE)) {
            definition.setAllowMultiple(true);
        }
        if (templatedQuestion.has(SURVEY_JS_SHOW_OTHER)) {
            definition.setAllowOtherDescription(templatedQuestion.get(SURVEY_JS_SHOW_OTHER).asBoolean());
        }

        //For normal elements, we'll store the title in the question_text column
        //For HTML elements which don't have a title, we'll store the HTML instead
        if (templatedQuestion.has("title")) {
            definition.setQuestionText(templatedQuestion.get("title").asText());
        } else if (templatedQuestion.has("html")) {
            definition.setQuestionText(templatedQuestion.get("html").asText());
        }

        if (templatedQuestion.has("isRequired")) {
            definition.setRequired(templatedQuestion.get("isRequired").asBoolean());
        }

        if (templatedQuestion.has("choices")) {
            definition.setChoices(unmarshalSurveyQuestionChoices(templatedQuestion));
        }

        return definition;
    }

    public static String unmarshalSurveyQuestionChoices(JsonNode question) {
        List<QuestionChoice> choices = new ArrayList<>();
        for (JsonNode choice : question.get("choices")) {
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

    /**
     * gets any calculated value nodes that should be included in results
     */
    public static List<JsonNode> getCalculatedValues(JsonNode surveyJsDef) {
        List<JsonNode> calculatedValues = new ArrayList<>();
        if (surveyJsDef.has("calculatedValues")) {
            for (JsonNode val : surveyJsDef.get("calculatedValues")) {
                if (Boolean.TRUE.equals(val.get("includeIntoResult").asBoolean())) {
                    calculatedValues.add(val);
                }
            }
        }
        return calculatedValues;
    }

    /**
     * returns the last stableId that this calculatedValue is dependent on, or null
     * if it is independent.
     * e.g. if the expression is "{heightInInches} * 2.54", this will return "heightInInches"
     */
    public static String getUpstreamStableId(JsonNode calculatedValue) {
        String expression = calculatedValue.get("expression").asText();
        Matcher matcher = EXPRESSION_DEPENDENCY.matcher(expression);
        matcher.find();
        return matcher.matches() ? matcher.group(1).trim() : null;
    }

    /**
     * This method is used to get the answer to a question by its stableId
     * The method will return the answer as the specified class in returnClass
     * The method will return null if the questionStableIdL is not found in the surveyJsonData
     * @param surveyJsonData the survey json data
     *                       The method assumes the surveyJsonData is a valid survey json of an array of questions with different stableIds
     * @param questionStableIdL the stableId of the question to get the answer for
     * @param returnClass the class to return the answer as
     *                    The method assumes the returnClass is a valid class that can be used to convert the answer to
     * @param objectMapper the object mapper to use to convert the answer to the returnClass
     * @param <T> the class to return the answer as
     * @return the answer to the question with the stableId questionStableIdL as the class returnClass
     * */
    public static <T> T getAnswerByStableId(String surveyJsonData, String questionStableIdL, Class<T> returnClass,
                                            ObjectMapper objectMapper) {
        try {
            JsonNode rootNode = objectMapper.readTree(surveyJsonData);
            if (rootNode.isArray()) {
                for (JsonNode node : rootNode) {
                    if (questionStableIdL.equals(getQuestionStableId(node))) {
                        return convertNodeToClass(node, returnClass, objectMapper);

                    }
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * This method is used to convert the answer to a survey to the returnClass. The method assumes the answer is a valid and is
     * in the objectValue field of the node
     * */
    static <T> T convertNodeToClass(JsonNode node, Class<T> returnClass, ObjectMapper objectMapper) throws JsonProcessingException {
        // Assuming `node` is the JsonNode containing the stringified array in `objectValue`
        String objectValueString = node.get("objectValue").asText();
        List<String> objectValues = objectMapper.readValue(objectValueString, new TypeReference<List<String>>() {});
        String valueToConvert = objectValues.get(0);
        // Direct conversion for String
        if (returnClass == String.class) {
            return returnClass.cast(valueToConvert);
        }

        // Attempt to use a constructor that takes a single String argument for other types
        try {
            Constructor<T> constructor = returnClass.getConstructor(String.class);
            return constructor.newInstance(valueToConvert);
        } catch (Exception e) {
            throw new IllegalArgumentException("The provided returnClass does not have a String constructor that we can use.", e);
        }
    }

    static String getQuestionStableId(JsonNode node) {
        JsonNode questionStableIdNode = node.get("questionStableId");
        return questionStableIdNode != null ? questionStableIdNode.asText() : null;
    }

}
