package bio.terra.pearl.core.service.export.formatters.item;

import bio.terra.pearl.core.model.survey.*;
import bio.terra.pearl.core.service.export.BaseExporter;
import bio.terra.pearl.core.service.export.DataValueExportType;
import bio.terra.pearl.core.service.export.ExportOptions;
import bio.terra.pearl.core.service.export.formatters.ExportFormatUtils;
import bio.terra.pearl.core.service.export.formatters.module.ModuleFormatter;
import bio.terra.pearl.core.service.export.formatters.module.SurveyFormatter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@SuperBuilder
@Getter
public class AnswerItemFormatter extends ItemFormatter<SurveyResponse> {
    private ObjectMapper objectMapper;

    private String questionStableId;
    private String questionType;
    private String questionText;
    @Builder.Default
    private boolean splitOptionsIntoColumns = false;
    @Builder.Default
    private boolean stableIdsForOptions = true;
    @Builder.Default
    private boolean hasOtherDescription = false;
    @Builder.Default
    private List<QuestionChoice> choices = null;

    @Builder.Default
    private boolean isChildQuestion = false;
    private String parentStableId;
    private Integer repeatIndex;

    /**
     * for survey questions, we need to have a map of past versions so we can look up values of stableIds that may
     * no longer be supported, and also to produce a full data dictionary
     */
    @Builder.Default
    private Map<Integer, AnswerItemFormatter> versionMap = new HashMap<>();

    /**
     * takes a list of all versions of a question (all sharing a question stableId) and returns an ItemExportInfo
     * specifying export column(s) information.  This ItemExportInfo will have child ItemExportInfos for each
     * version of the question, so that the exporter can map answers to choices from all versions of the question
     */
    public AnswerItemFormatter(
            ExportOptions exportOptions,
            String moduleName,
            List<SurveyQuestionDefinition> questionVersions,
            ObjectMapper objectMapper,
            Integer repeatIndex) {
        this(exportOptions,
                moduleName,
                questionVersions.stream()
                        .sorted(Comparator.comparingInt(SurveyQuestionDefinition::getSurveyVersion).reversed())
                        .findFirst().orElseThrow(() -> new IllegalArgumentException("Empty list of question versions")),
                objectMapper,
                repeatIndex);
        for (SurveyQuestionDefinition questionDef : questionVersions) {
            this.getVersionMap().put(questionDef.getSurveyVersion(), new AnswerItemFormatter(exportOptions, moduleName, questionDef, objectMapper, repeatIndex));
        }
    }

    public AnswerItemFormatter(
            ExportOptions exportOptions,
            String moduleName,
            List<SurveyQuestionDefinition> questionVersions,
            ObjectMapper objectMapper) {
        this(exportOptions, moduleName, questionVersions, objectMapper, null);
    }

    /**
     * takes a single version of a question and returns an ItemExportInfo specifying export column(s) info
     */
    public AnswerItemFormatter(
            ExportOptions exportOptions,
            String moduleName,
            SurveyQuestionDefinition questionDef,
            ObjectMapper objectMapper,
            Integer repeatIndex) {
        List<QuestionChoice> choices = new ArrayList<>();
        if (questionDef.getChoices() != null) {
            try {
                choices = objectMapper.readValue(questionDef.getChoices(), new TypeReference<List<QuestionChoice>>(){});
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Error parsing choices for question " + questionDef.getQuestionStableId(), e);
            }
        }
        boolean splitOptions = exportOptions.isSplitOptionsIntoColumns() && choices.size() > 0 && questionDef.isAllowMultiple();
        this.repeatIndex = repeatIndex;
        baseColumnKey = buildBaseKey(questionDef.getQuestionStableId(), repeatIndex, questionDef.getParentStableId());
        questionStableId = questionDef.getQuestionStableId();
        stableIdsForOptions = exportOptions.isStableIdsForOptions();
        splitOptionsIntoColumns = splitOptions;
        allowMultiple = questionDef.isAllowMultiple();
        this.choices = choices;
        /**
         * For now, all survey answers are exported as strings.  We will likely revisit this later, but this
         * gives much more robustness with respect to representing "prefer not to answer" than trying to convert
         * every possible value of a question to a number/date
         */
        dataType = isAllowMultiple() || questionDef.getQuestionType().equals("matrix") ? DataValueExportType.OBJECT_STRING : DataValueExportType.STRING;
        questionType = questionDef.getQuestionType();
        questionText = questionDef.getQuestionText();
        hasOtherDescription = questionDef.isAllowOtherDescription();
        isChildQuestion = questionDef.getParentStableId() != null;
        parentStableId = questionDef.getParentStableId();
        versionMap = new HashMap<>();
        this.objectMapper = objectMapper;
    }

    private static String buildBaseKey(String questionStableId, Integer repeatIndex, String parentStableId) {
        String key = questionStableId;
        if (Objects.nonNull(repeatIndex)) {
            key += ExportFormatUtils.formatIndex(repeatIndex);
        }
        if (StringUtils.isNotEmpty(parentStableId)) {
            key = parentStableId + ExportFormatUtils.COLUMN_NAME_DELIMITER + key;
        }

        return key;
    }


    @Override
    public void applyToEveryColumn(BaseExporter.ColumnProcessor columnProcessor, ModuleFormatter moduleFormatter, int moduleRepeatNum) {
        if (isSplitOptionsIntoColumns()) {
            // add a column for each option
            for (QuestionChoice choice : getChoices()) {
                columnProcessor.apply(moduleFormatter, this, false, choice, moduleRepeatNum);
            }
        } else {
            columnProcessor.apply(moduleFormatter, this,false, null, moduleRepeatNum);
        }
        if (isHasOtherDescription()) {
            // for questions with free-text other, we add an additional column to capture that value
            columnProcessor.apply(moduleFormatter, this, true, null, moduleRepeatNum);
        }
    }

    @Override
    public String getEmptyValue() {
        if (isSplitOptionsIntoColumns()) {
            return SurveyFormatter.SPLIT_OPTION_UNSELECTED_VALUE;
        } else {
            return super.getEmptyValue();
        }
    }

    @Override
    public void importValueToBean(SurveyResponse response, String exportString) {
        if (exportString == null) {
            // we don't create empty answers if the participant doesn't have a value specified
            return;
        }
        Answer answer = Answer.builder()
                .questionStableId(questionStableId)
                .build();
        if (dataType.equals(DataValueExportType.OBJECT_STRING)) {
            answer.setAnswerType(AnswerType.OBJECT);
            answer.setObjectValue(exportString);
        } else {
            answer.setValueAndType(ExportFormatUtils.getValueFromString(exportString, dataType));
        }
        response.getAnswers().add(answer);
    }
}
