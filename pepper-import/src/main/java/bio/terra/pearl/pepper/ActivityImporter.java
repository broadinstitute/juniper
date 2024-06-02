package bio.terra.pearl.pepper;

import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.participant.RandomUtilService;
import bio.terra.pearl.pepper.dto.SurveyJSContent;
import bio.terra.pearl.pepper.dto.SurveyJSQuestion;
import bio.terra.pearl.populate.dto.survey.SurveyPopDto;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.broadinstitute.ddp.model.activity.definition.question.PicklistOptionDef;
import org.broadinstitute.ddp.model.activity.definition.question.PicklistQuestionDef;
import org.broadinstitute.ddp.model.activity.definition.question.QuestionDef;
import org.broadinstitute.ddp.model.activity.definition.question.TextQuestionDef;
import org.broadinstitute.ddp.model.activity.types.BlockType;
import org.broadinstitute.ddp.model.activity.types.PicklistRenderMode;
import org.broadinstitute.ddp.model.activity.types.PicklistSelectMode;
import org.broadinstitute.ddp.model.activity.types.QuestionType;
import org.broadinstitute.ddp.studybuilder.translation.I18nReader;
import org.broadinstitute.ddp.studybuilder.translation.TranslationsProcessingData;
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
    private final RandomUtilService randomUtilService;
    private final String[] languages = {"en", "es", "de", "fr", "hi", "it", "ja", "pl", "pt", "ru", "tr", "zh"};

    public ActivityImporter(ObjectMapper objectMapper, RandomUtilService randomUtilService) {
        this.objectMapper = objectMapper;
        this.randomUtilService = randomUtilService;
    }

    public Survey parsePepperForm(Config varsConfig, Path dirPath, Path path) throws JsonProcessingException {
        File file = dirPath.resolve(path).toFile();
        if (!file.exists()) {
            throw new RuntimeException("Activity definition file is missing: " + file);
        }

        I18nReader i18nReader = new I18nReader();
        Map<String, TranslationsProcessingData.TranslationData> i18nTranslations = i18nReader.readTranslationsFromFilesInSpecifiedFolder(dirPath + "/i18n");

        FormActivityDef activityDef = buildActivity(file, FormActivityDef.class, varsConfig);
        Map<String, Map<String, Object>> languageTranslations = new HashMap<>();
        for (String language : languages) {
            Map<String, Object> langMap = varsConfig.getConfig("i18n." + language).entrySet().stream()
                    .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
            languageTranslations.put(language, langMap);
        }

        Map<String, Object> langMap = varsConfig.getConfig("i18n.en").entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
        return convert(activityDef, langMap);
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

    public SurveyPopDto convert(FormActivityDef activityDef, Map<String, Object> langMap) throws JsonProcessingException {
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
                    JsonNode contentNode = getJsonNodeForContentBlock(langMap, (ContentBlockDef) blockDef);
                    elements.add(contentNode);
                }

                if (blockDef.getBlockType().equals(BlockType.GROUP)) {
                    GroupBlockDef groupBlockDef = ((GroupBlockDef) blockDef);
                    List<FormBlockDef> nestedBlockdefs = groupBlockDef.getNested();
                    for (FormBlockDef nestedBlockDef : nestedBlockdefs) {
                        if (nestedBlockDef.getBlockType().equals(BlockType.CONTENT)) {
                            JsonNode contentNode = getJsonNodeForContentBlock(langMap, (ContentBlockDef) nestedBlockDef);
                            elements.add(contentNode);
                        }
                    }
                }

                //todo .. add expressions / calculatedValues
                //todo dynamic text.. conditional logic..
                for (QuestionDef pepperQuestionDef : blockDef.getQuestions().toList()) {
                    String questionText = i18nContentRenderer.renderToString(pepperQuestionDef.getPromptTemplate().getTemplateText(), langMap);
                    if (StringUtils.isEmpty(questionText)) {
                        if (pepperQuestionDef.getQuestionType().equals(QuestionType.TEXT)) {
                            TextQuestionDef textQuestionDef = (TextQuestionDef) pepperQuestionDef;
                            questionText = i18nContentRenderer.renderToString(textQuestionDef.getPlaceholderTemplate().getTemplateText(), langMap);
                        }
                    }
                    Map<String, String> titleMap = new HashMap<>();
                    titleMap.put("en", questionText);
                    String questionType = pepperQuestionDef.getQuestionType().name();
                    String inputType = null;
                    if (questionType.equalsIgnoreCase("DATE")) {
                        questionType = "TEXT";
                        inputType = "DATE";
                    }
                    List<SurveyJSQuestion.Choice> choices = null;
                    if (questionType.equalsIgnoreCase("PICKLIST")) {
                        choices = new ArrayList<>();
                        questionType = "dropdown";
                        inputType = null;
                        PicklistQuestionDef picklistQuestionDef = (PicklistQuestionDef) pepperQuestionDef;
                        if (picklistQuestionDef.getRenderMode() == PicklistRenderMode.LIST
                                && picklistQuestionDef.getSelectMode() == PicklistSelectMode.SINGLE
                                && picklistQuestionDef.getRenderMode() != PicklistRenderMode.DROPDOWN) {
                            questionType = "radiogroup";
                        }
                        if (picklistQuestionDef.getRenderMode() == PicklistRenderMode.LIST && picklistQuestionDef.getSelectMode() == PicklistSelectMode.MULTIPLE) {
                            questionType = "checkbox";
                        }
                        for (PicklistOptionDef option : picklistQuestionDef.getPicklistOptions()) {
                            ObjectNode choiceNode = objectMapper.createObjectNode();
                            choiceNode.put("value", option.getStableId());
                            String optTxt = i18nContentRenderer.renderToString(option.getOptionLabelTemplate().getTemplateText(), langMap);
                            if (optTxt != null && optTxt.startsWith("$")) {
                                optTxt = option.getOptionLabelTemplate().getVariables().stream().findAny().get().getTranslation("en").get().getText();
                            }
                            choices.add(new SurveyJSQuestion.Choice(optTxt, option.getStableId()));
                        }
                    }

                    //todo calculated values
                    List<SurveyJSQuestion.CalculatedValue> calculatedValues = null;
                    if (!StringUtils.isEmpty(blockDef.getShownExpr())) {
                        calculatedValues = new ArrayList<>();
                        //populate calculated values
                        String name = null; //todo
                        String expression = blockDef.getShownExpr();
                        //todo for now using pepper expressions. need to convert pepper expression into SurveyJS format
                        SurveyJSQuestion.CalculatedValue calculatedValue = new SurveyJSQuestion.CalculatedValue(name, expression, "true");
                        calculatedValues.add(calculatedValue);
                    }

                    SurveyJSQuestion surveyJSQuestion = SurveyJSQuestion.builder()
                            .name(pepperQuestionDef.getStableId())
                            .type(questionType)
                            .title(titleMap)
                            .required(false) //todo
                            //.isRequired() //todo
                            .inputType(inputType)
                            .choices(choices)
                            .calculatedValues(calculatedValues)
                            .build();

                    JsonNode questionNode = objectMapper.valueToTree(surveyJSQuestion);
                    elements.add(questionNode);
                }
            }
        }
        survey.setJsonContent(root);
        return survey;
    }

    private JsonNode getJsonNodeForContentBlock(Map<String, Object> langMap, ContentBlockDef blockDef) {
        ContentBlockDef contentBlockDef = blockDef;
        String titleTemplateTxt = contentBlockDef.getTitleTemplate() != null ? contentBlockDef.getTitleTemplate().getTemplateText() : null;
        String bodyTemplateTxt = contentBlockDef.getBodyTemplate() != null ? contentBlockDef.getBodyTemplate().getTemplateText() : null;
        //log.info("Title template: {}", titleTemplateTxt); //todo title template ?
        //log.info("Body template: {}", bodyTemplateTxt);
        String tmplText = i18nContentRenderer.renderToString(bodyTemplateTxt, langMap);
        Map<String, String> titleMap = new HashMap<>();
        titleMap.put("en", tmplText);
        String name = contentBlockDef.getBodyTemplate().getVariables().stream().findAny().get().getName();
        SurveyJSContent surveyJSContent = SurveyJSContent.builder()
                .name(name) //todo generate name
                .type("html")
                .html(titleMap)
                .build();
        JsonNode contentNode = objectMapper.valueToTree(surveyJSContent);
        return contentNode;
    }

}
