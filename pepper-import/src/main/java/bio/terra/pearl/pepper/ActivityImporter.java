package bio.terra.pearl.pepper;

import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyType;
import bio.terra.pearl.pepper.dto.SurveyJSContent;
import bio.terra.pearl.pepper.dto.SurveyJSPanel;
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
import org.broadinstitute.ddp.model.activity.definition.template.Template;
import org.broadinstitute.ddp.model.activity.definition.template.TemplateVariable;
import org.broadinstitute.ddp.model.activity.types.*;
import org.broadinstitute.ddp.util.ConfigUtil;
import org.broadinstitute.ddp.util.GsonUtil;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ActivityImporter {
    private final Gson gson = GsonUtil.standardGson();
    private final ObjectMapper objectMapper;
    private final I18nContentRenderer i18nContentRenderer = new I18nContentRenderer();
    private final String[] languages = {"en", "es"};


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
                .surveyType(getSurveyType(activityDef))
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

        SurveyImportContext surveyImportContext = new SurveyImportContext();
        surveyImportContext.setFormStableId(activityDef.getActivityCode());
        surveyImportContext.setAllLangMap(allLangMap);

        for (FormSectionDef section : activityDef.getAllSections()) {
            ObjectNode page = objectMapper.createObjectNode();
            pages.add(page);
            ArrayNode elements = objectMapper.createArrayNode();
            page.set("elements", elements);
            for (FormBlockDef blockDef : section.getBlocks()) {
                elements.addAll(convertBlock(surveyImportContext, blockDef));
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

    private List<JsonNode> convertBlock(SurveyImportContext ctx, FormBlockDef blockDef) {
        // originally, we just used the "getQuestions" method on the blockDef
        // to handle nested questions, but this doesn't surface any of the
        // nested conditional visibility expressions, so we have to
        // manually search through the question tree
        List<JsonNode> elements = new ArrayList<>();

        switch (blockDef.getBlockType()) {
            case QUESTION:
                elements.addAll(convertBlockQuestions(ctx, blockDef));
                break;
            case CONTENT:
                elements.addAll(getJsonNodeForContentBlock(ctx, (ContentBlockDef) blockDef));
                break;
            case COMPONENT:
                elements.addAll(getJsonNodeForComponentBlock(ctx, (ComponentBlockDef) blockDef));
                break;
            case GROUP:
                elements.addAll(convertGroupBlock(ctx, blockDef));
                break;
            case CONDITIONAL:
                elements.addAll(convertConditionalBlock(ctx, (ConditionalBlockDef) blockDef));
                break;
            default:
                log.warn("Unsupported block type: " + blockDef.getBlockType());
        }
        return elements;
    }

    private List<JsonNode> convertGroupBlock(SurveyImportContext ctx, FormBlockDef blockDef) {
        List<JsonNode> nodes = new ArrayList<>();
        if (blockDef.getBlockType().equals(BlockType.GROUP)) {
            GroupBlockDef groupBlockDef = ((GroupBlockDef) blockDef);
            List<FormBlockDef> nestedBlockdefs = groupBlockDef.getNested();
            for (FormBlockDef nestedBlockDef : nestedBlockdefs) {
                nestedBlockDef.setShownExpr(concatShownExpr(blockDef.getShownExpr(), nestedBlockDef.getShownExpr()));
                nodes.addAll(convertBlock(ctx, nestedBlockDef));
            }
        }

        SurveyJSPanel panel = SurveyJSPanel.builder()
                .type("panel")
                .elements(nodes)
                .visibleIf(convertVisibilityExpressions(blockDef.getShownExpr()))
                .build();

        return List.of(objectMapper.valueToTree(panel));
    }

    private List<JsonNode> convertConditionalBlock(SurveyImportContext ctx, ConditionalBlockDef blockDef) {
        List<JsonNode> elements = new ArrayList<>();
        if (blockDef.getBlockType().equals(BlockType.CONDITIONAL)) {

            elements.addAll(convertQuestionToSurveyJsFormat(ctx, blockDef, blockDef.getControl()));

            for (FormBlockDef formBlockDef : blockDef.getNested()) {
                formBlockDef.setShownExpr(concatShownExpr(blockDef.getShownExpr(), formBlockDef.getShownExpr()));
                elements.addAll(convertBlock(ctx, formBlockDef));
            }
        }
        return elements;
    }

    private String concatShownExpr(String... shownExprs) {
        return Arrays.stream(shownExprs)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.joining(" && "));
    }

    private List<JsonNode> convertBlockQuestions(SurveyImportContext ctx, FormBlockDef blockDef) {
        List<JsonNode> questionNodes = new ArrayList<>();
        if (blockDef.getBlockType().equals(BlockType.QUESTION)) {
            questionNodes.addAll(convertQuestionToSurveyJsFormat(ctx, blockDef, ((QuestionBlockDef) blockDef).getQuestion()));
            return questionNodes;
        }

        return questionNodes;
    }

    private List<JsonNode> convertQuestionToSurveyJsFormat(SurveyImportContext ctx, FormBlockDef blockDef, QuestionDef pepperQuestionDef) {
        if (pepperQuestionDef.getQuestionType().equals(QuestionType.COMPOSITE)) {
            // composite questions are not 'questions' in surveyjs, rather panels.
            // so, they need to be handled totally differently
            return List.of(convertCompositeQuestion(ctx, blockDef, (CompositeQuestionDef) pepperQuestionDef));
        }

        Map<String, String> titleMap = getQuestionTxt(pepperQuestionDef);
        boolean titleIsEmpty = titleMap.isEmpty() || titleMap.values().stream().allMatch(StringUtils::isEmpty);

        String questionType = getQuestionType(pepperQuestionDef);
        String inputType = null;

        Map<String, String> placeholder = null;
        if (pepperQuestionDef.getQuestionType().equals(QuestionType.DATE)) {
            inputType = "date";
            DateQuestionDef dateQuestionDef = (DateQuestionDef) pepperQuestionDef;
            if (Objects.nonNull(dateQuestionDef.getPlaceholderTemplate())) {
                placeholder = translatePepperTemplate(dateQuestionDef.getPlaceholderTemplate());
            }
        }

        if (pepperQuestionDef.getQuestionType().equals(QuestionType.NUMERIC)) {
            inputType = "number";
            NumericQuestionDef numericQuestionDef = (NumericQuestionDef) pepperQuestionDef;
            if (Objects.nonNull(numericQuestionDef.getPlaceholderTemplate())) {
                placeholder = translatePepperTemplate(numericQuestionDef.getPlaceholderTemplate());
            }
        }

        if (pepperQuestionDef.getQuestionType().equals(QuestionType.TEXT)) {
            TextQuestionDef textQuestionDef = (TextQuestionDef) pepperQuestionDef;
            if (Objects.nonNull(textQuestionDef.getPlaceholderTemplate())) {
                placeholder = translatePepperTemplate(textQuestionDef.getPlaceholderTemplate());
            }
        }

        List<SurveyJSQuestion.Choice> choices = null;

        List<JsonNode> otherQuestions = new ArrayList<>();
        if (pepperQuestionDef.getQuestionType().equals(QuestionType.PICKLIST)) {
            PicklistQuestionDef picklistQuestionDef = (PicklistQuestionDef) pepperQuestionDef;
            choices = getPicklistChoices(picklistQuestionDef, ctx.getAllLangMap());

            // confusingly, label is placeholder for picklists
            if (Objects.nonNull(picklistQuestionDef.getPicklistLabelTemplate())) {
                placeholder = translatePepperTemplate(picklistQuestionDef.getPicklistLabelTemplate());
            }

            // surveyjs only supports 1 other question at a time, but pepper often has many other
            // questions to just one picklist
            otherQuestions = createOtherQuestions(ctx, picklistQuestionDef, choices);
        }

        Map<String, String> labelTrue = null;
        Map<String, String> labelFalse = null;
        String valueTrue = null;
        String valueFalse = null;
        if (pepperQuestionDef.getQuestionType().equals(QuestionType.BOOLEAN)) {
            BoolQuestionDef boolQuestionDef = (BoolQuestionDef) pepperQuestionDef;

            labelTrue = translatePepperTemplate(boolQuestionDef.getTrueTemplate());
            labelFalse = translatePepperTemplate(boolQuestionDef.getFalseTemplate());
            valueTrue = "Yes";
            valueFalse = "No";
        } else if (pepperQuestionDef.getQuestionType().equals(QuestionType.AGREEMENT)) {
            valueTrue = "Yes";
            valueFalse = "No";
        }

        if (titleMap.isEmpty() && placeholder != null) {
            // in certain cases, pepper likes to put the title in the
            // placeholder where the placeholder would be invisible
            // in surveyjs, e.g. date, so let's put it in both to be safe
            titleMap = placeholder;
        }

        SurveyJSContent tooltip = null;
        if (Objects.nonNull(pepperQuestionDef.getTooltipTemplate())) {
            // let's just render it so we get translations - if we need to use it,
            // we can figure out where this goes in the future, but at least we have it.
            tooltip = SurveyJSContent.builder()
                    .name(pepperQuestionDef.getStableId() + "_TOOLTIP")
                    .type("html")
                    .html(translatePepperTemplate(pepperQuestionDef.getTooltipTemplate()))
                    .build();
        }


        SurveyJSQuestion surveyJSQuestion = SurveyJSQuestion.builder()
                .name(pepperQuestionDef.getStableId())
                .type(questionType)
                .titleLocation(titleIsEmpty ? "hidden" : null)
                .title(titleMap)
                .placeholder(placeholder)
                .labelTrue(labelTrue)
                .labelFalse(labelFalse)
                .valueTrue(valueTrue)
                .valueFalse(valueFalse)
                .inputType(inputType)
                .choices(choices)
                .visibleIf(convertVisibilityExpressions(blockDef.getShownExpr()))
                .build();
        ValidationConverter.applyValidation(pepperQuestionDef, surveyJSQuestion);


        List<JsonNode> out = new ArrayList<>();
        if (Objects.nonNull(tooltip)) {
            out.add(objectMapper.valueToTree(tooltip));
        }
        out.add(objectMapper.valueToTree(surveyJSQuestion));

        if (!otherQuestions.isEmpty()) {
            out.addAll(otherQuestions);
        }

        return out;
    }

    // pepper does not require content (html/text) stable ids to be unique, but
    // juniper does. this function ensures uniqueness by appending _1, _2, etc. to the stable id,
    // should only be used for content blocks
    private String ensureUniqueContentBlockStableIds(SurveyImportContext ctx, String stableId) {
        String uniqueStableId = stableId;
        int counter = 1;
        while (ctx.getContentBlockStableIds().contains(uniqueStableId)) {
            uniqueStableId = stableId + "_" + counter;
            System.out.println("WARNING: duplicate stableId found: " + stableId + ", renaming to " + uniqueStableId);
            counter++;
        }
        ctx.getContentBlockStableIds().add(uniqueStableId);
        return uniqueStableId;
    }

    private List<JsonNode> createOtherQuestions(SurveyImportContext ctx, PicklistQuestionDef picklistQuestionDef, List<SurveyJSQuestion.Choice> choices) {

        boolean hasDetailsAllowed = picklistQuestionDef.getPicklistOptions().stream().anyMatch(PicklistOptionDef::isDetailsAllowed);

        if (!hasDetailsAllowed) {
            return List.of();
        }

        if (picklistQuestionDef.getSelectMode().equals(PicklistSelectMode.SINGLE)) {

            PicklistOptionDef option = picklistQuestionDef.getPicklistOptions().stream().filter(PicklistOptionDef::isDetailsAllowed).findFirst().orElseThrow();

            Map<String, String> otherTitle = translatePepperTemplate(option.getDetailLabelTemplate());
            String detailStableId = picklistQuestionDef.getStableId() + "_DETAIL";

            SurveyJSQuestion otherQuestion = SurveyJSQuestion.builder()
                    .name(detailStableId)
                    .type("text")
                    .title(otherTitle)
                    .visibleIf("{" + picklistQuestionDef.getStableId() + "} contains '" + option.getStableId() + "'")
                    .validators(null)
                    .build();

            return List.of(objectMapper.valueToTree(otherQuestion));
        }

        List<JsonNode> otherQuestions = new ArrayList<>();
        for (PicklistOptionDef option : picklistQuestionDef.getPicklistOptions()) {
            if (option.isDetailsAllowed()) {
                Map<String, String> otherTitle = translatePepperTemplate(option.getDetailLabelTemplate());

                // this is the naming convention for these questions in DSM
                String detailStableId = picklistQuestionDef.getStableId() + "_" + option.getStableId() + "_DETAIL";

                SurveyJSQuestion otherQuestion = SurveyJSQuestion.builder()
                        .name(detailStableId)
                        .type("text")
                        .title(otherTitle)
                        .visibleIf("{" + picklistQuestionDef.getStableId() + "} contains '" + option.getStableId() + "'")
                        .validators(null)
                        .build();
                otherQuestions.add(objectMapper.valueToTree(otherQuestion));

            }
        }
        return otherQuestions;
    }

    private JsonNode convertCompositeQuestion(SurveyImportContext ctx, FormBlockDef blockDef, CompositeQuestionDef pepperQuestionDef) {

        // add button template is the text of the add button
        Map<String, String> addButtonTemplate = null;
        if (Objects.nonNull(pepperQuestionDef.getAddButtonTemplate())) {
            addButtonTemplate = translatePepperTemplate(pepperQuestionDef.getAddButtonTemplate());
        }

        // additional item template is the title above every new item the user
        // adds, e.g. "Other Medication" in the Medication question
        Map<String, String> additionalItemTemplate = null;
        if (Objects.nonNull(pepperQuestionDef.getAdditionalItemTemplate())) {
            additionalItemTemplate = translatePepperTemplate(pepperQuestionDef.getAdditionalItemTemplate());
        }

        // find all subquestions for the composite question
        List<JsonNode> subQuestions = pepperQuestionDef.getChildren().stream()
                .flatMap(child -> convertQuestionToSurveyJsFormat(ctx, blockDef, child).stream())
                .collect(Collectors.toList());

        SurveyJSPanel panel;
        if (pepperQuestionDef.isAllowMultiple()) {
            // construct as surveyjs panel dynamic section
            panel = SurveyJSPanel
                    .builder()
                    .name(pepperQuestionDef.getStableId())
                    .type("paneldynamic")
                    .title(getQuestionTxt(pepperQuestionDef))
                    .templateElements(subQuestions)
                    .panelAddText(addButtonTemplate)
                    .templateTitle(additionalItemTemplate)
                    .visibleIf(convertVisibilityExpressions(blockDef.getShownExpr()))
                    .build();
        } else {
            subQuestions = subQuestions.stream().map(node -> {
                ObjectNode nodeObj = (ObjectNode) node;
                nodeObj.put("name", pepperQuestionDef.getStableId() + "_" + node.get("name").asText());
                return nodeObj;
            }).collect(Collectors.toList());

            panel = SurveyJSPanel
                    .builder()
                    .name(pepperQuestionDef.getStableId())
                    .title(getQuestionTxt(pepperQuestionDef))
                    .elements(subQuestions)
                    .visibleIf(convertVisibilityExpressions(blockDef.getShownExpr()))
                    .build();
        }


        return objectMapper.valueToTree(panel);
    }

    public static Map<String, String> translatePepperTemplate(Template template) {
        return getVariableTranslationsTxt(template.getTemplateText(),
                template.getVariables());
    }

    private String getQuestionType(QuestionDef pepperQuestionDef) {
        QuestionType questionType = pepperQuestionDef.getQuestionType();
        String surveyJsType = "text";
        if (questionType.equals(QuestionType.DATE) || questionType.equals(QuestionType.NUMERIC)) {
            surveyJsType = "text";
        }
        if (questionType.equals(QuestionType.AGREEMENT)) {
            surveyJsType = "boolean";
        }
        if (questionType.equals(QuestionType.BOOLEAN)) {
            surveyJsType = "boolean";
        }
        if (questionType.equals(QuestionType.PICKLIST)) {
            surveyJsType = "dropdown";
            PicklistQuestionDef picklistQuestionDef = (PicklistQuestionDef) pepperQuestionDef;
            if (picklistQuestionDef.getRenderMode().equals(PicklistRenderMode.LIST)) {
                if (picklistQuestionDef.getSelectMode().equals(PicklistSelectMode.SINGLE)) {
                    surveyJsType = "radiogroup";
                } else {
                    surveyJsType = "checkbox";
                }
            }
        }

        if (pepperQuestionDef.getQuestionType().equals(QuestionType.TEXT)) {
            TextQuestionDef textQuestionDef = (TextQuestionDef) pepperQuestionDef;
            if (textQuestionDef.getInputType().equals(TextInputType.SIGNATURE)) {
                surveyJsType = "signaturepad";
            }
            if (textQuestionDef.getInputType().equals(TextInputType.ESSAY)) {
                surveyJsType = "comment";
            }
        }

        return surveyJsType;

    }

    private Map<String, String> getQuestionTxt(QuestionDef pepperQuestionDef) {

        Map<String, String> txtMap = getVariableTranslationsTxt(pepperQuestionDef.getPromptTemplate().getTemplateText(),
                pepperQuestionDef.getPromptTemplate().getVariables());
        if (txtMap.isEmpty() && pepperQuestionDef.getQuestionType().name().equalsIgnoreCase("TEXT")) {
            TextQuestionDef textQuestionDef = (TextQuestionDef) pepperQuestionDef;
            //try placeholder template
            if (textQuestionDef.getPlaceholderTemplate() != null) {
                txtMap = getVariableTranslationsTxt(textQuestionDef.getPlaceholderTemplate().getTemplateText(),
                        textQuestionDef.getPlaceholderTemplate().getVariables());
            }
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

        List<TemplateVariable> templateVariableCopy = new ArrayList<>(templateVariables);

        // sort variable names by length to ensure that situations like, e.g., $diagnosis_center and $diagnosis_center_country
        // are handled in the correct order
        templateVariableCopy.sort(Comparator.comparingInt(var -> -1 * var.getName().length()));

        // for each variable, get the translations and replace the variable name with the translation text in the appropriate language
        for (TemplateVariable var : templateVariableCopy) {
            for (Translation translation : var.getTranslations()) {
                String languageText = textMap.getOrDefault(translation.getLanguageCode(), StringUtils.trim(templateText));
                languageText = languageText.replace("$" + var.getName(), translation.getText());
                textMap.put(translation.getLanguageCode(), languageText);
            }
        }
        return textMap;
    }


    private List<SurveyJSQuestion.Choice> getPicklistChoices(PicklistQuestionDef picklistQuestionDef, Map<String, Map<String, Object>> allLangMap) {

        List<SurveyJSQuestion.Choice> choices = new ArrayList<>();
        for (PicklistOptionDef option : picklistQuestionDef.getPicklistOptions()) {
            ObjectNode choiceNode = objectMapper.createObjectNode();
            choiceNode.put("value", option.getStableId());

            Map<String, String> choiceTranslations = translatePepperTemplate(option.getOptionLabelTemplate());
            choices.add(new SurveyJSQuestion.Choice(choiceTranslations, option.getStableId()));
        }
        return choices;
    }


    private Map<String, String> fromPepperTranslations(List<Translation> translations) {
        return translations.stream()
                .collect(Collectors.toMap(Translation::getLanguageCode, Translation::getText));
    }


    private List<JsonNode> getJsonNodeForContentBlock(SurveyImportContext ctx, ContentBlockDef blockDef) {
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

        String name = ensureUniqueContentBlockStableIds(ctx, contentBlockDef.getBodyTemplate().getVariables().stream().findAny().get().getName());

        List<JsonNode> out = new ArrayList<>();
        if (!titleTxtMap.isEmpty()) {
            SurveyJSContent titleContent = SurveyJSContent.builder()
                    .name(name + "_title")
                    .type("html")
                    .html(titleTxtMap)
                    .visibleIf(convertVisibilityExpressions(blockDef.getShownExpr()))
                    .build();
            out.add(objectMapper.valueToTree(titleContent));
        }
        SurveyJSContent surveyJSContent = SurveyJSContent.builder()
                .name(name)
                .type("html")
                .html(htmlTxtMap)
                .visibleIf(convertVisibilityExpressions(blockDef.getShownExpr()))
                .build();
        out.add(objectMapper.valueToTree(surveyJSContent));
        return out;
    }

    private List<JsonNode> getJsonNodeForComponentBlock(SurveyImportContext ctx, ComponentBlockDef blockDef) {
        ComponentType componentType = blockDef.getComponentType();
        return List.of(); // todo implement this
    }


    public String convertVisibilityExpressions(String pepperExpr) {
        if (StringUtils.isEmpty(pepperExpr)) {
            return null;
        }

        Pattern questionPattern = Pattern.compile("user\\.studies\\[\"(.*?)\"\\]\\.forms\\[\"(.*?)\"\\]\\.questions\\[\"(.*?)\"\\]\\.(.*?)\\((.*?)\\)");
        // study e.g.: !user.studies["atcp"].isGovernedParticipant()
        Pattern studyPattern = Pattern.compile("user\\.studies\\[\"(.*?)\"\\]\\.(.*?)\\(\\)");
        String out = questionPattern.matcher(pepperExpr).replaceAll(matchResult -> {
            String study = matchResult.group(1);
            String form = matchResult.group(2);
            String question = matchResult.group(3);
            String pepperOperation = matchResult.group(4);
            String value = matchResult.group(5);
            String stableId = "{" + question + "}";
            String loweredOperation = pepperOperation.toLowerCase().trim();

            switch (loweredOperation) {
                case "answers.hasoption" -> pepperOperation = "contains";
                case "answers.hastrue" -> {
                    return "(" + stableId + " = true or " + stableId + " = 'Yes')";
                }
                case "isanswered" -> {
                    return stableId + " notempty";
                }
                case "answers.hasanyoption" -> {
                    return convertVisibilityExpressionHasAnyOption(question, value);
                }
                default -> throw new RuntimeException("Unsupported pepper operation: " + pepperOperation);
            }
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            return stableId + " " + pepperOperation + " '" + value + "'";
        });

        out = studyPattern.matcher(out).replaceAll(matchResult -> {
            String study = matchResult.group(1);
            String pepperOperation = matchResult.group(2);
            switch (pepperOperation.toLowerCase().trim()) {
                case "isgovernedparticipant":
                    return "{isGovernedUser} = true";
                default:
                    throw new RuntimeException("Unsupported pepper operation: " + pepperOperation);
            }
        });

        return out.replace("&&", "and").replace("\n", "").trim();

    }

    private String convertVisibilityExpressionHasAnyOption(String questionStableId, String value) {
        // the value is a comma-separated list of options with quotes around each
        // e.g.  "option1","option2","option3"
        // with brackets, becomes valid json array
        List<String> options;
        try {
            JsonNode node = objectMapper.readTree("[" + value + "]");
            if (node.isArray()) {
                options = new ArrayList<>();
                for (JsonNode optionNode : node) {
                    options.add(optionNode.asText());
                }
            } else {
                throw new RuntimeException("Expected an array for hasAnyOption value: " + value);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing hasAnyOption value: " + value, e);
        }
        if (options.isEmpty()) {
            return "1=0"; // always false
        }
        return "(" + options.stream().map(opt -> "{" + questionStableId + "} contains '" + opt + "'").collect(Collectors.joining(" or ")) + ")";
    }

    SurveyType getSurveyType(FormActivityDef activityDef) {
        if (Objects.requireNonNull(activityDef.getFormType()).equals(FormType.CONSENT)) {
            return SurveyType.CONSENT;
        }

        return SurveyType.RESEARCH;
    }

    /** maps pepper replacement vars to Juniper vars, and html markup to markdown.
     * After testing some more surveys, we might want to upgrade this to use a html->markdown parsing library */
    static Map<String, String> JUNIPER_PEPPER_STRING_MAP = Map.ofEntries(
            Map.entry("\\$ddp.participantFirstName\\(\\)", "{profile.givenName}"),
/**
 * If uncommented, all the HTML tags will be stripped from the survey. Not always helpful - without
 * a much more complex system, it removes tags in places that are sometimes helpful. Depends on the survey
 * which is easier to clean up.
 */
//            Map.entry("\\<p.*?\\>", ""),
//            Map.entry("\\</p\\>", "\\\\n"),
//            Map.entry("\\<span.*?\\>", ""),
//            Map.entry("\\</span\\>", ""),
//            Map.entry("\\<b.*?\\>", "**"),
//            Map.entry("\\</b\\>", "**"),
//            Map.entry("<b.*?>", "**"),
//            Map.entry("</b>", "**"),
//            Map.entry("\\<i.*?\\>", "*"),
//            Map.entry("\\</i\\>", "*"),
//            Map.entry("<i.*?>", "*"),
//            Map.entry("</i>", "*"),
//            Map.entry("\\<em.*?\\>", "*"),
//            Map.entry("\\</em\\>", "*"),
            Map.entry(" +", " ")
    );

}
