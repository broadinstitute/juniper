package bio.terra.pearl.pepper;

import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.pepper.dto.SurveyJSContent;
import bio.terra.pearl.pepper.dto.SurveyJSQuestion;
import bio.terra.pearl.populate.dto.survey.SurveyPopDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigResolveOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.broadinstitute.ddp.content.I18nContentRenderer;
import org.broadinstitute.ddp.model.activity.definition.*;
import org.broadinstitute.ddp.model.activity.definition.i18n.Translation;
import org.broadinstitute.ddp.model.activity.definition.question.*;
import org.broadinstitute.ddp.model.activity.definition.template.TemplateVariable;
import org.broadinstitute.ddp.model.activity.types.BlockType;
import org.broadinstitute.ddp.model.activity.types.PicklistRenderMode;
import org.broadinstitute.ddp.model.activity.types.PicklistSelectMode;
import org.broadinstitute.ddp.util.ConfigUtil;
import org.broadinstitute.ddp.util.GsonUtil;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ActivityImporter {
    private final Gson gson = GsonUtil.standardGson();
    private final ObjectMapper objectMapper;
    private final I18nContentRenderer i18nContentRenderer = new I18nContentRenderer();
    private final String[] languages = {"en", "es", "de", "fr", "hi", "it", "ja", "pl", "pt", "ru", "tr", "zh"};


    public ActivityImporter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Survey parsePepperForm(Config varsConfig, Path dirPath, Path path) {
        File file = dirPath.resolve(path).toFile();
        if (!file.exists()) {
            throw new RuntimeException("Activity definition file is missing: " + file);
        }

        FormActivityDef activityDef = buildActivity(file, FormActivityDef.class, varsConfig);
        Map<String, Map<String, Object>> languageTranslations = new HashMap<>();
        for (String language : languages) {
            Map<String, Object> langMap = varsConfig.getConfig("i18n." + language).entrySet().stream()
                    .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
            languageTranslations.put(language, langMap);
        }

        return convert(activityDef, languageTranslations);
    }

    public <T> T buildActivity(File file, Class<T> targetClass, Config varsConfig) {
        Config definition = ConfigFactory.parseFile(file)
                // going to resolve first the external global variables that might be used in this configuration
                // using setAllowUnresolved = true so we can do a second pass that will allow us to resolve variables
                // within the configuration
                .resolveWith(varsConfig, ConfigResolveOptions.defaults().setAllowUnresolved(false));
        if (definition.isEmpty()) {
            throw new RuntimeException("Activity definition file is empty: " + file);
        }

        T activityDef = gson.fromJson(ConfigUtil.toJson(definition), targetClass);
        return activityDef;
    }

    public SurveyPopDto convert(FormActivityDef activityDef, Map<String, Map<String, Object>> allLangMap) {
        Map<String, String> translatedSurveyTitles = fromPepperTranslations(activityDef.getTranslatedNames());

        SurveyPopDto survey = SurveyPopDto.builder()
                .stableId(activityDef.getActivityCode())
                .version(1)
                .name(translatedSurveyTitles.get("en"))
                .build();

        ObjectNode root = objectMapper.createObjectNode();

        // it seems like in pepper, they redefine the title in multiple places
        // for example, medical_title and medical_name are the same thing.
        // I can't find any surveys that define them differently so we're
        // just using the one here.
        root.set("title", objectMapper.valueToTree(translatedSurveyTitles));

        // there are other properties, e.g. description, that we could set here
        // but they seem to be unused in pepper

        ArrayNode pages = root.putArray("pages");
        for (FormSectionDef section : activityDef.getAllSections()) {
            ObjectNode page = objectMapper.createObjectNode();
            pages.add(page);
            ArrayNode elements = objectMapper.createArrayNode();
            page.set("elements", elements);
            for (FormBlockDef blockDef : section.getBlocks()) {
                //content blocks
                if (blockDef.getBlockType().equals(BlockType.CONTENT)) {
                    JsonNode contentNode = getJsonNodeForContentBlock(allLangMap, (ContentBlockDef) blockDef);
                    elements.add(contentNode);
                }

                elements.addAll(getNestedContent(allLangMap, blockDef));

                //need to handle expressions / calculatedValues .. dynamic text.. conditional logic.
                elements.addAll(convertBlockQuestions(allLangMap, blockDef));
            }
        }
        try {
            String surveyJson = objectMapper.writeValueAsString(root);
            for (Map.Entry<String, String> entry : JUNIPER_PEPPER_STRING_MAP.entrySet()) {
                surveyJson = surveyJson.replaceAll(entry.getKey(), entry.getValue());
            }
            survey.setJsonContent(objectMapper.readTree(surveyJson));
        } catch (Exception e) {
            throw new RuntimeException("Error converting survey to JSON", e);
        }
        return survey;
    }

    private List<JsonNode> getNestedContent(Map<String, Map<String, Object>> allLangMap, FormBlockDef blockDef) {
        List<JsonNode> contentNodes = new ArrayList<>();
        if (blockDef.getBlockType().equals(BlockType.GROUP)) {
            GroupBlockDef groupBlockDef = ((GroupBlockDef) blockDef);
            List<FormBlockDef> nestedBlockdefs = groupBlockDef.getNested();
            for (FormBlockDef nestedBlockDef : nestedBlockdefs) {
                if (nestedBlockDef.getBlockType().equals(BlockType.CONTENT)) {
                    JsonNode contentNode = getJsonNodeForContentBlock(allLangMap, (ContentBlockDef) nestedBlockDef);
                    contentNodes.add(contentNode);
                }
            }
        }
        return contentNodes;
    }

    private List<JsonNode> convertBlockQuestions(Map<String, Map<String, Object>> allLangMap, FormBlockDef blockDef) {
        List<JsonNode> questionNodes = new ArrayList<>();
        for (QuestionDef pepperQuestionDef : blockDef.getQuestions().toList()) {

            questionNodes.add(convertQuestionToSurveyJsFormat(blockDef, allLangMap, pepperQuestionDef));
        }
        return questionNodes;
    }

    private JsonNode convertQuestionToSurveyJsFormat(FormBlockDef blockDef, Map<String, Map<String, Object>> allLangMap, QuestionDef pepperQuestionDef) {
        Map<String, String> titleMap = getQuestionTxt(pepperQuestionDef);
        String questionType = getQuestionType(pepperQuestionDef);
        String inputType = null;
        if (pepperQuestionDef.getQuestionType().name().equalsIgnoreCase("DATE")) {
            inputType = "DATE";
        }
        if (pepperQuestionDef.getQuestionType().name().equalsIgnoreCase("NUMERIC")) {
            inputType = "NUMBER";
        }

        if (pepperQuestionDef.getQuestionType().name().equalsIgnoreCase("COMPOSITE")) {
            // composite questions are not 'questions' in surveyjs, rather panels.
            // so, they need to be formatted differently
            return convertCompositeQuestion(blockDef, allLangMap, (CompositeQuestionDef) pepperQuestionDef);
        }

        List<SurveyJSQuestion.Choice> choices = null;
        if (pepperQuestionDef.getQuestionType().name().equalsIgnoreCase("PICKLIST")) {
            choices = getPicklistChoices((PicklistQuestionDef) pepperQuestionDef, allLangMap);
        }

        //expression  revisit and try this
        //parse the pepper expression for stableID and value/option and generate Juniper expression
        //EX: user.studies[\"atcp\"].forms[\"REGISTRATION\"].questions[\"REGISTRATION_COUNTRY\"].answers.hasOption (\"AF\")
        //parse value after questions[] and hasOption and generate
        //"visibleIf": "{REGISTRATION_COUNTRY} contains 'AF'"
        //works only for picklist/choices though

        SurveyJSQuestion surveyJSQuestion = SurveyJSQuestion.builder()
                .name(pepperQuestionDef.getStableId())
                .type(questionType)
                .title(titleMap)
                .isRequired(false)
                .inputType(inputType)
                .choices(choices)
                .visibleIf(blockDef.getShownExpr())
                .build();
        ValidationConverter.applyValidation(pepperQuestionDef, surveyJSQuestion);

        return objectMapper.valueToTree(surveyJSQuestion);
    }

    private JsonNode convertCompositeQuestion(FormBlockDef blockDef, Map<String, Map<String, Object>> allLangMap, CompositeQuestionDef pepperQuestionDef) {

        // add button template is the text of the add button
        Map<String, String> addButtonTemplate = null;
        if (Objects.nonNull(pepperQuestionDef.getAddButtonTemplate())) {
            addButtonTemplate = getVariableTranslationsTxt(pepperQuestionDef.getAddButtonTemplate().getTemplateText(),
                    pepperQuestionDef.getAddButtonTemplate().getVariables());
        }

        // additional item template is the title above every new item the user
        // adds, e.g. "Other Medication" in the Medication question
        Map<String, String> additionalItemTemplate = null;
        if (Objects.nonNull(pepperQuestionDef.getAdditionalItemTemplate())) {
            additionalItemTemplate = getVariableTranslationsTxt(pepperQuestionDef.getAdditionalItemTemplate().getTemplateText(),
                    pepperQuestionDef.getAdditionalItemTemplate().getVariables());
        }

        // find all subquestions for the composite question
        List<JsonNode> subQuestions = pepperQuestionDef.getChildren().stream()
                .map(child -> convertQuestionToSurveyJsFormat(blockDef, allLangMap, child))
                .collect(Collectors.toList());


        // construct as surveyjs panel dynamic section
        Map<String, Object> compositeQuestionMap = new HashMap<>();
        compositeQuestionMap.put("name", pepperQuestionDef.getStableId());
        compositeQuestionMap.put("type", "paneldynamic");
        compositeQuestionMap.put("title", getQuestionTxt(pepperQuestionDef));
        compositeQuestionMap.put("templateElements", subQuestions);
        compositeQuestionMap.put("panelAddText", addButtonTemplate);
        compositeQuestionMap.put("templateTitle", additionalItemTemplate);
        compositeQuestionMap.put("visibleIf", blockDef.getShownExpr());
        return objectMapper.valueToTree(compositeQuestionMap);
    }

    private String getQuestionType(QuestionDef pepperQuestionDef) {
        String questionType = pepperQuestionDef.getQuestionType().name();
        if (questionType.equalsIgnoreCase("DATE") || questionType.equalsIgnoreCase("NUMERIC")
                || questionType.equalsIgnoreCase("COMPOSITE")) {
            questionType = "TEXT";
        }
        if (questionType.equalsIgnoreCase("AGREEMENT")) {
            questionType = "boolean";
        }
        List<SurveyJSQuestion.Choice> choices = null;
        if (questionType.equalsIgnoreCase("PICKLIST")) {
            questionType = "dropdown";
            PicklistQuestionDef picklistQuestionDef = (PicklistQuestionDef) pepperQuestionDef;
            if (picklistQuestionDef.getRenderMode() == PicklistRenderMode.LIST
                    && picklistQuestionDef.getSelectMode() == PicklistSelectMode.SINGLE
                    && picklistQuestionDef.getRenderMode() != PicklistRenderMode.DROPDOWN) {
                questionType = "radiogroup";
            }
            if (picklistQuestionDef.getRenderMode() == PicklistRenderMode.LIST && picklistQuestionDef.getSelectMode() == PicklistSelectMode.MULTIPLE) {
                questionType = "checkbox";
            }
        }

        return questionType;

    }

    private Map<String, String> getQuestionTxt(QuestionDef pepperQuestionDef) {
        Map<String, String> txtMap = getVariableTranslationsTxt(pepperQuestionDef.getPromptTemplate().getTemplateText(),
                pepperQuestionDef.getPromptTemplate().getVariables());
        if (txtMap.isEmpty() && pepperQuestionDef.getQuestionType().name().equalsIgnoreCase("TEXT")) {
            TextQuestionDef textQuestionDef = (TextQuestionDef) pepperQuestionDef;
            //try placeholder template
            txtMap = getVariableTranslationsTxt(textQuestionDef.getPlaceholderTemplate().getTemplateText(),
                    textQuestionDef.getPlaceholderTemplate().getVariables());
        }


        return txtMap;
    }

    /**
     * Template texts are typically constructed as variables with formatting markup between them
     * so we need to replace the variable name with the translation text in the appropriate language,
     * but preserve the markup
     */
    public static Map<String, String> getVariableTranslationsTxt(String templateText, Collection<TemplateVariable> templateVariables) {
        Map<String, String> textMap = new HashMap<>();
        // for each variable, get the translations and replace the variable name with the translation text in the appropriate language
        for (TemplateVariable var : templateVariables) {
            for (Translation translation : var.getTranslations()) {
                String languageText = textMap.getOrDefault(translation.getLanguageCode(), StringUtils.trim(templateText));
                textMap.put(translation.getLanguageCode(), languageText.replace( "$" + var.getName(), translation.getText()));
            }
        }
        return textMap;
    }


    private List<SurveyJSQuestion.Choice> getPicklistChoices(PicklistQuestionDef picklistQuestionDef, Map<String, Map<String, Object>> allLangMap) {
        List<SurveyJSQuestion.Choice> choices = new ArrayList<>();
        for (PicklistOptionDef option : picklistQuestionDef.getPicklistOptions()) {
            ObjectNode choiceNode = objectMapper.createObjectNode();
            choiceNode.put("value", option.getStableId());
            Map<String, String> choiceTranslations = new HashMap<>();
            for (String lang : allLangMap.keySet()) {
                String optTxt = i18nContentRenderer.renderToString(option.getOptionLabelTemplate().getTemplateText(), allLangMap.get(lang));
                if (optTxt != null && optTxt.startsWith("$")) {
                    Translation translation = option.getOptionLabelTemplate().getVariables().stream().findAny().get().getTranslation(lang).orElse(null);
                    if (translation != null) {
                        optTxt = option.getOptionLabelTemplate().getVariables().stream().findAny().get().getTranslation(lang).get().getText();
                        choiceTranslations.put(lang, optTxt);
                    }
                }
            }
            choices.add(new SurveyJSQuestion.Choice(choiceTranslations, option.getStableId()));
        }
        return choices;
    }

    private Map<String, String> fromPepperTranslations(List<Translation> translations) {
        return translations.stream()
                .collect(Collectors.toMap(Translation::getLanguageCode, Translation::getText));
    }


    private JsonNode getJsonNodeForContentBlock(Map<String, Map<String, Object>> allLangMap, ContentBlockDef blockDef) {
        ContentBlockDef contentBlockDef = blockDef;
        String titleTemplateTxt = contentBlockDef.getTitleTemplate() != null ? contentBlockDef.getTitleTemplate().getTemplateText() : null; //where to set this title txt ?
        String bodyTemplateTxt = contentBlockDef.getBodyTemplate() != null ? contentBlockDef.getBodyTemplate().getTemplateText() : null;
        Map<String, String> htmlTxtMap = new HashMap<>();
        Map<String, String> titleTxtMap = new HashMap<>();
        if (!StringUtils.isEmpty(bodyTemplateTxt) && bodyTemplateTxt.contains("$")) {
            //get txt from variables
            htmlTxtMap = getVariableTranslationsTxt(contentBlockDef.getBodyTemplate().getTemplateText(), contentBlockDef.getBodyTemplate().getVariables());
        }

        if (!StringUtils.isEmpty(titleTemplateTxt) && titleTemplateTxt.contains("$")) {
            titleTxtMap = getVariableTranslationsTxt(contentBlockDef.getTitleTemplate().getTemplateText(), contentBlockDef.getTitleTemplate().getVariables());
        }

        String name = contentBlockDef.getBodyTemplate().getVariables().stream().findAny().get().getName();
        SurveyJSContent surveyJSContent = SurveyJSContent.builder()
                .name(name)
                .type("html")
                .html(htmlTxtMap)
                .title(titleTxtMap.isEmpty() ? null : titleTxtMap)
                .build();
        return objectMapper.valueToTree(surveyJSContent);
    }

    /** maps pepper replacement vars to Juniper vars, and html markup to markdown.
     * After testing some more surveys, we might want to upgrade this to use a html->markdown parsing library */
    Map<String, String> JUNIPER_PEPPER_STRING_MAP = Map.of(
            "\\$ddp.participantFirstName\\(\\)", "{proxyProfile.givenName}",
            "\\<p.*?\\>", "",
            "\\</p\\>", "\\\\n",
            "\\<em.*?\\>", "**",
            "\\</em\\>", "**",
            " +", " "
    );

}
