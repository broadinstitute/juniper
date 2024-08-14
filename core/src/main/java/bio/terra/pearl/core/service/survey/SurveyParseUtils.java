package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.model.i18n.LanguageText;
import bio.terra.pearl.core.model.survey.QuestionChoice;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyQuestionDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Constructor;
import java.util.*;
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
    /** should match sanitizeStableId in NewQuestionForm.tsx */
    public static final Pattern INVALID_STABLE_ID = Pattern.compile("[^a-zA-Z\\-_\\d]");

    /**
     * recursively gets all questions from the given node.
     */

    public static List<JsonNode> getAllQuestions(JsonNode containerElement) {
        List<JsonNode> elements = new ArrayList<>();
        if (containerElement.has("elements")) {
            for (JsonNode element : containerElement.get("elements")) {
                elements.addAll(getAllQuestions(element));
            }
        } else {
            elements.add(containerElement);
            // certain questions have multiple subquestions (e.g., paneldynamic or matrix)
            elements.addAll(getSubQuestions(containerElement));
        }

        return elements;
    }

    private static List<JsonNode> getSubQuestions(JsonNode parent) {
        if (!parent.has("type")) {
            return List.of();
        }

        List<JsonNode> subQuestions = new ArrayList<>();

        if (parent.get("type").asText().equals("paneldynamic") && parent.has("templateElements")) {
            subQuestions = getPanelDynamicSubQuestions(parent);
        }

        // keep track of the parent stableid
        subQuestions = subQuestions
                .stream()
                .map(q -> (JsonNode) q.deepCopy())
                .map(q -> {
                    ((ObjectNode) q).put("parent", parent.get("name").asText());
                    return q;
                })
                .toList();
        return subQuestions;
    }

    private static List<JsonNode> getPanelDynamicSubQuestions(JsonNode parent) {
        List<JsonNode> subQuestions = new ArrayList<>();

        for (JsonNode subQuestion : parent.get("templateElements")) {
            List<JsonNode> questions = getAllQuestions(subQuestion);
            subQuestions.addAll(questions);
        }
        return subQuestions;
    }

    public static SurveyQuestionDefinition unmarshalSurveyQuestion(
            Survey survey,
            JsonNode question,
            Map<String, JsonNode> questionTemplates,
            int globalOrder,
            boolean isDerived) {

        SurveyQuestionDefinition definition = SurveyQuestionDefinition.builder()
                .surveyId(survey.getId())
                .surveyStableId(survey.getStableId())
                .surveyVersion(survey.getVersion())
                .questionStableId(question.get("name").asText())
                .exportOrder(globalOrder)
                .parentStableId(question.has("parent") ? question.get("parent").asText() : null)
                .repeatable(question.has("type") && question.get("type").asText().equals("paneldynamic"))
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

    /** confirm the question definition meets our (currently very permissive) requirements */
    public static void validateQuestionDefinition(SurveyQuestionDefinition surveyQuestionDefinition, List<SurveyQuestionDefinition> allDefsInSurvey) {
        /** we don't care about the stableIds for html questions, since those aren't answered and aren't included in data exports */
        if (!List.of("html").contains(surveyQuestionDefinition.getQuestionType())) {
            validateQuestionStableId(surveyQuestionDefinition.getQuestionStableId());
        }

        if (surveyQuestionDefinition.getParentStableId() != null) {
            SurveyQuestionDefinition parent = allDefsInSurvey.stream()
                    .filter(d -> d.getQuestionStableId().equals(surveyQuestionDefinition.getParentStableId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Parent question not found: " + surveyQuestionDefinition.getParentStableId()));

            if (StringUtils.isNotEmpty(parent.getParentStableId())) {
                throw new IllegalArgumentException("Deeply nested questions are not supported: " + surveyQuestionDefinition.getQuestionStableId());
            }
        }

    }
    public static void validateQuestionStableId(String questionStableId) {
        if (questionStableId == null || questionStableId.isBlank()) {
            throw new IllegalArgumentException("Question stableId cannot be null or empty");
        }
        if (INVALID_STABLE_ID.matcher(questionStableId).matches()) {
            throw new IllegalArgumentException("Question stableId must be alphanumeric, dashes or underscores: '" + questionStableId + "'");
        }
    }

    public static String unmarshalSurveyQuestionChoices(JsonNode question) {
        List<QuestionChoice> choices = new ArrayList<>();
        for (JsonNode choice : question.get("choices")) {
            // if all text/value pairs are the same, surveyjs transforms the choices into an array of strings.  grrrr...
            if (choice.isTextual()) {
                choices.add(new QuestionChoice(choice.asText(), choice.asText()));
            } else {
                JsonNode textNode = choice.get("text");

                String text;
                if (textNode.isTextual()) {
                    text = textNode.asText();
                } else {
                    if (textNode.has("en")) {
                        text = textNode.get("en").asText();
                    } else {
                        text = textNode.get("value").asText();
                    }
                }

                choices.add(new QuestionChoice(choice.get("value").asText(), text));
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
                JsonNode includeIntoResult = val.get("includeIntoResult");
                if (includeIntoResult != null && Boolean.TRUE.equals(includeIntoResult.asBoolean())) {
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
     * The method will return null if the questionStableId is not found in the surveyJsonData
     * @param surveyJsonData the survey json data
     *                       The method assumes the surveyJsonData is a valid survey json of an array of questions with different stableIds
     * @param questionStableId the stableId of the question to get the answer for
     * @param returnClass the class to return the answer as
     *                    The method assumes the returnClass is a valid class that can be used to convert the answer to
     * @param objectMapper the object mapper to use to convert the answer to the returnClass
     * @param answerField the field to get the answer from in the question node. If null, it will attempt to get the answer from the first non-null field.
     * @param <T> the class to return the answer as
     * @return the answer to the question with the stableId questionStableId as the class returnClass
     * */
    public static <T> T getAnswerByStableId(String surveyJsonData, String questionStableId, Class<T> returnClass,
                                            ObjectMapper objectMapper, String answerField) {
        try {
            JsonNode rootNode = objectMapper.readTree(surveyJsonData);
            if (rootNode.isArray()) {
                for (JsonNode node : rootNode) {
                    if (questionStableId.equals(getQuestionStableId(node))) {
                        return convertQuestionAnswerToClass(node, answerField, returnClass, objectMapper);
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
     * in the answerField of the node
     * */
    protected static <T> T convertQuestionAnswerToClass(JsonNode node, String answerField, Class<T> returnClass, ObjectMapper objectMapper) throws JsonProcessingException {
        String objectValueString;
        if (Objects.nonNull(answerField)) {
            objectValueString = node.get(answerField).asText();
        } else {
            objectValueString = getAnswerValue(node);
        }
        // Direct conversion for String
        if (returnClass == String.class) {
            return returnClass.cast(objectValueString);
        }
        String value = objectMapper.readValue(objectValueString, String.class);
        // Attempt to use a constructor that takes a single String argument for other types
        try {
            Constructor<T> constructor = returnClass.getConstructor(String.class);
            return constructor.newInstance(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("The provided returnClass does not have a String constructor that we can use.", e);
        }
    }

    private static String getAnswerValue(JsonNode node) {
        for (String valueField : List.of("stringValue", "booleanValue", "objectValue", "numberValue")) {
            JsonNode valueNode = node.get(valueField);
            if (valueNode != null) {
                return valueNode.asText();
            }
        }
        throw new IllegalArgumentException("Could not find a value in the answer node");
    }

    protected static String getQuestionStableId(JsonNode node) {
        JsonNode questionStableIdNode = node.get("questionStableId");
        return questionStableIdNode != null ? questionStableIdNode.asText() : null;
    }

    /** extracts LanguageTexts suitable for rendering the survey title in the participant UI from the survey content */
    public static List<LanguageText> extractLanguageTexts(Survey survey) {
        Map<String, String> parsedTitles = SurveyParseUtils.parseSurveyTitle(survey.getContent(), survey.getName());
        return SurveyParseUtils.titlesToLanguageTexts(
                SurveyParseUtils.formToLanguageTextKey(survey.getStableId(),survey.getVersion()),
                survey.getPortalId(),
                parsedTitles);
    }

    //Returns a Map of languageCode -> title for the survey
    public static Map<String, String> parseSurveyTitle(String formContent, String formName) {
        if(formContent == null) {
            //If the form content is empty, there won't be any titles to parse, so fall back to the formName
            return Map.of("en", formName);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode surveyContent;

        try {
            surveyContent = objectMapper.readTree(formContent);
        } catch (JsonProcessingException e) {
            //If we can't parse the actual form content, we'll fall back to the formName
            return Map.of("en", formName);
        }

        // A survey title can take 3 forms:
        // 1. null (default to the formName in this case)
        // 2. String value (survey has not been i18n'd)
        // 3. Map value (survey has been i18n'd)
        JsonNode title = surveyContent.get("title");
        if (title == null) {
            return Map.of("en", formName);
        }

        if(title.isTextual()) {
            return Map.of("en", title.asText());
        }

        if(title.isObject()) {
            Map<String, String> titleMap = new HashMap<>();
            Iterator<Map.Entry<String, JsonNode>> fields = title.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (field.getValue().isTextual()) {
                    String languageKey = field.getKey();
                    if(languageKey.equalsIgnoreCase("default")) {
                        //TODO (JN-863): this should set the key to the default portal language code
                        languageKey = "en";
                    }
                    titleMap.put(languageKey, field.getValue().asText());
                } else {
                    throw new IllegalArgumentException("Expected String value for field: " + field.getKey());
                }
            }
            return titleMap;
        }

        else {
            //The title was of an unexpected type, so fall back to the formName
            return Map.of("en", formName);
        }
    }

    public static List<LanguageText> titlesToLanguageTexts(String keyName, UUID portalId, Map<String, String> titles) {
        return titles.entrySet().stream().map(entry -> {
            LanguageText text = new LanguageText();
            text.setKeyName(keyName);
            text.setLanguage(entry.getKey());
            text.setText(entry.getValue());
            text.setPortalId(portalId);
            return text;
        }).toList();
    }

    public static String formToLanguageTextKey(String stableId, Integer version) {
        return String.format("%s:%s", stableId, version);
    }

}
