package bio.terra.pearl.pepper;

import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyQuestionDefinition;
import bio.terra.pearl.core.service.exception.internal.IOInternalException;
import bio.terra.pearl.populate.dto.survey.SurveyPopDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigResolveOptions;
import org.broadinstitute.ddp.content.I18nContentRenderer;
import org.broadinstitute.ddp.model.activity.definition.ActivityDef;
import org.broadinstitute.ddp.model.activity.definition.FormActivityDef;
import org.broadinstitute.ddp.model.activity.definition.FormBlockDef;
import org.broadinstitute.ddp.model.activity.definition.FormSectionDef;
import org.broadinstitute.ddp.model.activity.definition.question.QuestionDef;
import org.broadinstitute.ddp.model.activity.instance.FormSection;
import org.broadinstitute.ddp.studybuilder.ActivityBuilder;
import org.broadinstitute.ddp.util.ConfigUtil;
import org.broadinstitute.ddp.util.GsonUtil;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;

import javax.json.Json;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ActivityImporter {
    private final Gson gson = GsonUtil.standardGson();
    private final ObjectMapper objectMapper;
    private final I18nContentRenderer i18nContentRenderer = new I18nContentRenderer();

    public ActivityImporter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Survey parsePepperForm(Config varsConfig, Path dirPath, Path path) {
        File file = dirPath.resolve(path).toFile();
        if (!file.exists()) {
            throw new RuntimeException("Activity definition file is missing: " + file);
        }

        FormActivityDef activityDef = buildActivity(file, FormActivityDef.class, varsConfig);
        // for now just read english translations
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

    public SurveyPopDto convert(FormActivityDef activityDef, Map<String, Object> langMap) {
        SurveyPopDto survey = SurveyPopDto.builder()
                            .stableId(activityDef.getActivityCode())
                            .version(1)
                            .name(activityDef.getTag())
                            .build();

        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode pages = root.putArray("pages");
        for (FormSectionDef section: activityDef.getSections()) {
            ObjectNode page = objectMapper.createObjectNode();
            pages.add(page);
            ArrayNode elements = objectMapper.createArrayNode();
            page.set("elements", elements);
            for (FormBlockDef blockDef : section.getBlocks()) {
                ObjectNode panel = objectMapper.createObjectNode();
                elements.add(panel);
                ArrayNode panelElements = objectMapper.createArrayNode();
                panel.set("elements", panelElements);
                for (QuestionDef pepperQuestionDef : blockDef.getQuestions().toList()) {
                    String questionText = i18nContentRenderer.renderToString(pepperQuestionDef.getPromptTemplate().getTemplateText(), langMap);
                    SurveyQuestionDefinition questionDef = SurveyQuestionDefinition.builder()
                            .questionStableId(pepperQuestionDef.getStableId())
                            .questionText(questionText)
                            .questionType(pepperQuestionDef.getQuestionType().name())
                            .build();
                    JsonNode questionNode = objectMapper.valueToTree(questionDef);
                    panelElements.add(questionNode);
                }
            }
        }
        survey.setJsonContent(root);
        return survey;
    }



}
