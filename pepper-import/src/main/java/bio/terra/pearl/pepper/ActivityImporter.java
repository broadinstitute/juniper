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
import org.broadinstitute.ddp.model.activity.definition.ContentBlockDef;
import org.broadinstitute.ddp.model.activity.definition.FormActivityDef;
import org.broadinstitute.ddp.model.activity.definition.FormBlockDef;
import org.broadinstitute.ddp.model.activity.definition.FormSectionDef;
import org.broadinstitute.ddp.model.activity.definition.GroupBlockDef;
import org.broadinstitute.ddp.model.activity.definition.i18n.Translation;
import org.broadinstitute.ddp.model.activity.definition.question.BoolQuestionDef;
import org.broadinstitute.ddp.model.activity.definition.question.PicklistOptionDef;
import org.broadinstitute.ddp.model.activity.definition.question.PicklistQuestionDef;
import org.broadinstitute.ddp.model.activity.definition.question.QuestionDef;
import org.broadinstitute.ddp.model.activity.definition.question.TextQuestionDef;
import org.broadinstitute.ddp.model.activity.definition.template.TemplateVariable;
import org.broadinstitute.ddp.model.activity.types.BlockType;
import org.broadinstitute.ddp.model.activity.types.PicklistRenderMode;
import org.broadinstitute.ddp.model.activity.types.PicklistSelectMode;
import org.broadinstitute.ddp.model.activity.types.QuestionType;
import org.broadinstitute.ddp.util.ConfigUtil;
import org.broadinstitute.ddp.util.GsonUtil;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        SurveyPopDto survey = SurveyPopDto.builder()
                .stableId(activityDef.getActivityCode())
                .version(1)
                .name(activityDef.getTag())
                .build();

        ObjectNode root = objectMapper.createObjectNode();
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
        survey.setJsonContent(root);
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
            Map<String, String> titleMap = getTitleMap(allLangMap, pepperQuestionDef);
            String questionType = getQuestionType(pepperQuestionDef);
            String inputType = null;
            if (questionType.equalsIgnoreCase("DATE")) {
                inputType = "DATE";
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
                    .required(false)
                    //.isRequired() //revisit
                    .inputType(inputType)
                    .choices(choices)
                    .visibleIf(blockDef.getShownExpr())
                    .build();

            JsonNode questionNode = objectMapper.valueToTree(surveyJSQuestion);
            questionNodes.add(questionNode);
        }
        return questionNodes;
    }

    private String getQuestionType(QuestionDef pepperQuestionDef) {
        String questionType = pepperQuestionDef.getQuestionType().name();
        if (questionType.equalsIgnoreCase("DATE")) {
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

    private Map<String, String> getTitleMap(Map<String, Map<String, Object>> allLangMap, QuestionDef pepperQuestionDef) {
        Map<String, String> titleMap = new HashMap<>();
        for (String lang : allLangMap.keySet()) {
            String questionText = getQuestionTxt(allLangMap, pepperQuestionDef, lang);
            titleMap.put(lang, questionText);
        }
        return titleMap;
    }

    private String getQuestionTxt(Map<String, Map<String, Object>> allLangMap, QuestionDef pepperQuestionDef, String lang) {
        String questionText = i18nContentRenderer.renderToString(pepperQuestionDef.getPromptTemplate().getTemplateText(), allLangMap.get(lang));
        if (StringUtils.isEmpty(questionText) && pepperQuestionDef.getQuestionType().equals(QuestionType.TEXT)) {
            TextQuestionDef textQuestionDef = (TextQuestionDef) pepperQuestionDef;
            questionText = i18nContentRenderer.renderToString(textQuestionDef.getPlaceholderTemplate().getTemplateText(), allLangMap.get(lang));
        }

        if (questionText.contains("$")) {
            //get txt from variables
            if (pepperQuestionDef.getQuestionType().equals(QuestionType.TEXT)) {
                TextQuestionDef textQuestionDef = (TextQuestionDef) pepperQuestionDef;
                Translation translation = textQuestionDef.getPromptTemplate().getVariables().stream().findAny().get().getTranslation(lang).orElse(null);
                if (translation != null) {
                    questionText = translation.getText();
                }
            }
            if (pepperQuestionDef.getQuestionType().equals(QuestionType.BOOLEAN)) {
                BoolQuestionDef boolQuestionDef = (BoolQuestionDef) pepperQuestionDef;
                questionText = boolQuestionDef.getPromptTemplate().getVariables().stream().findFirst().get().getTranslation(lang).get().getText();
            }
        }
        return questionText;
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


    private JsonNode getJsonNodeForContentBlock(Map<String, Map<String, Object>> allLangMap, ContentBlockDef blockDef) {
        ContentBlockDef contentBlockDef = blockDef;
        String titleTemplateTxt = contentBlockDef.getTitleTemplate() != null ? contentBlockDef.getTitleTemplate().getTemplateText() : null; //where to set this title txt ?
        String bodyTemplateTxt = contentBlockDef.getBodyTemplate() != null ? contentBlockDef.getBodyTemplate().getTemplateText() : null;
        Map<String, String> htmlTxtMap = new HashMap<>();
        Map<String, String> titleTxtMap = new HashMap<>();
        if (!StringUtils.isEmpty(bodyTemplateTxt) && bodyTemplateTxt.contains("$")) {
            //get txt from variables
            for (TemplateVariable var : contentBlockDef.getBodyTemplate().getVariables()) {
                List<Translation> translations = var.getTranslations();
                for (Translation t : translations) {
                    htmlTxtMap.put(t.getLanguageCode(), t.getText());
                }
            }
        }
        for (String lang : allLangMap.keySet()) {
            String tmplText = i18nContentRenderer.renderToString(bodyTemplateTxt, allLangMap.get(lang));
            if (!StringUtils.isEmpty(tmplText) && !tmplText.contains("$")) {
                htmlTxtMap.put(lang, tmplText);
            }
            if (!StringUtils.isEmpty(titleTemplateTxt)) {
                String titleText = i18nContentRenderer.renderToString(titleTemplateTxt, allLangMap.get(lang));
                titleTxtMap.put(lang, titleText);
            }
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

}
