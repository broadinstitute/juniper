package bio.terra.pearl.core.service.export.formatters;

import bio.terra.pearl.core.model.survey.*;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.instance.ExportOptions;
import bio.terra.pearl.core.service.export.instance.ItemExportInfo;
import bio.terra.pearl.core.service.export.instance.ModuleExportInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import static java.util.stream.Collectors.groupingBy;

/**
 * See https://broad-juniper.zendesk.com/hc/en-us/articles/18259824756123-Participant-List-Export-details
 * for information on the export format of survey questions
 */
@Slf4j
public class SurveyFormatter implements ExportFormatter {
    public static String OTHER_DESCRIPTION_KEY_SUFFIX = "_description";
    public static String OTHER_DESCRIPTION_HEADER = "other description";
    public static String SPLIT_OPTION_SELECTED_VALUE = "1";
    public static String SPLIT_OPTION_UNSELECTED_VALUE = "0";
    private ObjectMapper objectMapper;

    public SurveyFormatter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, String> toStringMap(EnrolleeExportData exportData, ModuleExportInfo moduleInfo) throws Exception {
        Map<String, String> valueMap = new HashMap<>();
        String surveyStableId = moduleInfo.getModuleName();
        List<Answer> answers = exportData.getAnswers().stream().filter(ans ->
            Objects.equals(ans.getSurveyStableId(), surveyStableId)
        ).toList();
        // map the answers by question stable id for easier access
        Map<String, List<Answer>> answerMap = answers.stream().collect(groupingBy(Answer::getQuestionStableId));
        List<UUID> responseIds = answers.stream().map(Answer::getSurveyResponseId).distinct().toList();
        if (responseIds.isEmpty()) {
            return valueMap;
        }
        // for now, we only support exporting a single response per survey, so just grab the one that matches the first id
        SurveyResponse matchedResponse = exportData.getResponses().stream().filter(response ->
                response.getId().equals(responseIds.get(0))).findAny().orElse(null);
        if (matchedResponse == null) {
            return valueMap;
        }
        for (ItemExportInfo itemExportInfo : moduleInfo.getItems()) {
            if (itemExportInfo.getPropertyAccessor() != null) {
                // it's a property of the SurveyResponse
                ExportFormatUtils.addPropertyForExport(matchedResponse, itemExportInfo, valueMap);
            } else {
                // it's an answer value
                addAnswersToMap(moduleInfo, itemExportInfo, answerMap, valueMap);
            }
        }
        return valueMap;
    }

    @Override
    public String getColumnKey(ModuleExportInfo moduleExportInfo, ItemExportInfo itemExportInfo, boolean isOtherDescription, QuestionChoice choice) {
        if (isOtherDescription) {
            return itemExportInfo.getBaseColumnKey() + OTHER_DESCRIPTION_KEY_SUFFIX;
        } else if (itemExportInfo.isSplitOptionsIntoColumns()) {
            return itemExportInfo.getBaseColumnKey() + ExportFormatUtils.COLUMN_NAME_DELIMITER + choice.stableId();
        }
        return itemExportInfo.getBaseColumnKey();
    }

    /** this method largely mirrors "getColumnKey", but it strips out the prefixes to make the headers more readable */
    @Override
    public String getColumnHeader(ModuleExportInfo moduleExportInfo, ItemExportInfo itemExportInfo, boolean isOtherDescription, QuestionChoice choice) {
        String header = itemExportInfo.getBaseColumnKey();
        if (isOtherDescription) {
            return header + OTHER_DESCRIPTION_KEY_SUFFIX;
        } else if (itemExportInfo.getQuestionStableId() != null) {
            // for now, strip the prefixes to aid in readability.  Once we have multi-source surveys, we can revisit this.
            String cleanStableId = stripStudyAndSurveyPrefixes(itemExportInfo.getQuestionStableId());
            header = ExportFormatUtils.getColumnKey(moduleExportInfo.getModuleName(), cleanStableId);
            if (itemExportInfo.isSplitOptionsIntoColumns()) {
                header += ExportFormatUtils.COLUMN_NAME_DELIMITER + choice.stableId();
            }
        }
        return header;
    }

    /** returns either the question or the choice as friendly-ish text */
    @Override
    public String getColumnSubHeader(ModuleExportInfo moduleExportInfo, ItemExportInfo itemExportInfo, boolean isOtherDescription, QuestionChoice choice) {
        if (itemExportInfo.getPropertyAccessor() != null) {
            return ExportFormatUtils.camelToWordCase(itemExportInfo.getPropertyAccessor());
        }
        if (itemExportInfo.isSplitOptionsIntoColumns() && choice != null) {
            return choice.text();
        }
        if (isOtherDescription) {
            return OTHER_DESCRIPTION_HEADER;
        }
        return ExportFormatUtils.camelToWordCase(stripStudyAndSurveyPrefixes(itemExportInfo.getQuestionStableId()));
    }

    public ModuleExportInfo getModuleExportInfo(ExportOptions exportOptions, String stableId, List<Survey> surveys,
                                                List<SurveyQuestionDefinition> questionDefs) throws JsonProcessingException {
        String moduleName = stableId;
        List<ItemExportInfo> itemExportInfos = new ArrayList<>();
        itemExportInfos.add(ExportFormatUtils.getItemInfoForBeanProp(moduleName, "lastUpdatedAt", SurveyResponse.class));
        itemExportInfos.add(ExportFormatUtils.getItemInfoForBeanProp(moduleName, "complete", SurveyResponse.class));

        // group all the questions that share a stableId (i.e. different versions of the same question), and then sort them by
        // the export order of the most recent version
        Collection<List<SurveyQuestionDefinition>> questionDefsByStableId = questionDefs.stream().collect(groupingBy(
                SurveyQuestionDefinition::getQuestionStableId
        )).values().stream().sorted(Comparator.comparingInt(a -> a.get(0).getExportOrder())).toList();
        for (List<SurveyQuestionDefinition> questionVersions : questionDefsByStableId) {
            itemExportInfos.add(getItemExportInfo(exportOptions, moduleName, questionVersions));
        }

        // get the most recent survey by sorting in descending version order
        Survey latestSurvey = surveys.stream().sorted(Comparator.comparingInt(Survey::getVersion).reversed()).findFirst().get();

        return ModuleExportInfo.builder()
                .moduleName(moduleName)
                .displayName(latestSurvey.getName())
                .maxNumRepeats(1)
                .items(itemExportInfos)
                .formatter(this)
                .build();
    }

    /** strip out study and survey prefixes.  so e.g. "oh_oh_famHx_question1" becomes "question1" */
    public static String stripStudyAndSurveyPrefixes(String stableId) {
        // if there are >= 3 underscores, filter out everything before the third underscore
        int thirdUnderscoreIndex = StringUtils.ordinalIndexOf(stableId, "_", 3);
        if (thirdUnderscoreIndex < 0) {
            return stableId;
        }
        return stableId.substring(thirdUnderscoreIndex + 1);
    }

    /**
     * takes a list of all versions of a question (all sharing a question stableId) and returns an ItemExportInfo
     * specifying export column(s) information.  This ItemExportInfo will have child ItemExportInfos for each
     * version of the question, so that the exporter can map answers to choices from all versions of the question
     */
    protected ItemExportInfo getItemExportInfo(ExportOptions exportOptions, String moduleName, List<SurveyQuestionDefinition> questionVersions)
            throws JsonProcessingException {
        SurveyQuestionDefinition latestDef = questionVersions.stream()
                .sorted(Comparator.comparingInt(SurveyQuestionDefinition::getSurveyVersion).reversed()).findFirst().get();
        ItemExportInfo baseInfo = getItemExportInfo(exportOptions, moduleName, latestDef);
        for (SurveyQuestionDefinition questionDef : questionVersions) {
            baseInfo.getVersionMap().put(questionDef.getSurveyVersion(), getItemExportInfo(exportOptions, moduleName, questionDef));
        }
        return baseInfo;
    }

    /**
     * takes a single version of a question and returns an ItemExportInfo specifying export column(s) info
     */
    protected ItemExportInfo getItemExportInfo(ExportOptions exportOptions, String moduleName, SurveyQuestionDefinition questionDef)
            throws JsonProcessingException {
        List<QuestionChoice> choices = new ArrayList<>();
        if (questionDef.getChoices() != null) {
            choices = objectMapper.readValue(questionDef.getChoices(), new TypeReference<List<QuestionChoice>>(){});
        }
        boolean splitOptions = exportOptions.splitOptionsIntoColumns() && choices.size() > 0 && questionDef.isAllowMultiple();
        return ItemExportInfo.builder()
                .baseColumnKey(ExportFormatUtils.getColumnKey(moduleName, questionDef.getQuestionStableId()))
                .questionStableId(questionDef.getQuestionStableId())
                .stableIdsForOptions(exportOptions.stableIdsForOptions())
                .splitOptionsIntoColumns(splitOptions)
                .allowMultiple(questionDef.isAllowMultiple())
                .choices(choices)
                /**
                 * For now, all survey answers are exported as strings.  We will likely revisit this later, but this
                 * gives much more robustness with respect to representing "prefer not to answer" than trying to convert
                 * every possible value of a question to a number/date
                 */
                .dataType(DataValueExportType.STRING)
                .questionType(questionDef.getQuestionType())
                .questionText(questionDef.getQuestionText())
                .hasOtherDescription(questionDef.isAllowOtherDescription())
                .build();
    }

    public void addAnswersToMap(ModuleExportInfo moduleInfo, ItemExportInfo itemExportInfo,
                               Map<String, List<Answer>> answerMap, Map<String, String> valueMap) {
        List<Answer> matchedAnswers = answerMap.get(itemExportInfo.getQuestionStableId());
        if (matchedAnswers == null) {
            return;
        }
        // for now, we only support one answer per question, so just return the first
        Answer matchedAnswer = matchedAnswers.get(0);
        // use the ItemExport Info matching the answer version so choices get translated correctly
        ItemExportInfo matchedItemExportInfo = itemExportInfo.getVersionMap().get(matchedAnswer.getSurveyVersion());
        if (matchedItemExportInfo == null) {
            // if we can't find a match (likely because we're in a demo environment and the answer refers to a version that no longer exists)
            // just use the current version
            matchedItemExportInfo = itemExportInfo;
        }
        addAnswerToMap(moduleInfo, matchedItemExportInfo, matchedAnswer, valueMap, objectMapper);
    }

    protected static void addAnswerToMap(ModuleExportInfo moduleInfo, ItemExportInfo itemExportInfo,
                               Answer answer, Map<String, String> valueMap, ObjectMapper objectMapper) {
        if (itemExportInfo.isSplitOptionsIntoColumns()) {
            addSplitOptionSelectionsToMap(itemExportInfo, answer, valueMap, objectMapper);
        } else {
            valueMap.put(itemExportInfo.getBaseColumnKey(), valueAsString(answer, itemExportInfo.getChoices(),
                    itemExportInfo.isStableIdsForOptions(), objectMapper));
        }
        if (itemExportInfo.isHasOtherDescription() && answer.getOtherDescription() != null) {
            valueMap.put(itemExportInfo.getBaseColumnKey() + OTHER_DESCRIPTION_KEY_SUFFIX, answer.getOtherDescription());
        }
    }

    protected static String valueAsString(Answer answer, List<QuestionChoice> choices, boolean stableIdForOptions, ObjectMapper objectMapper) {
        if (answer.getStringValue() != null) {
            return formatStringValue(answer.getStringValue(), choices, stableIdForOptions, answer);
        } else if (answer.getBooleanValue() != null) {
            return answer.getBooleanValue() ? "true" : "false";
        } else if (answer.getNumberValue() != null) {
            return answer.getNumberValue().toString();
        } else if (answer.getObjectValue() != null) {
            return formatObjectValue(answer, choices, stableIdForOptions, objectMapper);
        }
        return "";
    }

    /** adds an entry to the valueMap for each selected option of a 'splitOptionsIntoColumns' question */
    protected static void addSplitOptionSelectionsToMap(ItemExportInfo itemExportInfo,
                                              Answer answer, Map<String, String> valueMap, ObjectMapper objectMapper) {
        if (answer.getStringValue() != null) {
            // this was a single-select question, so we only need to add the selected option
            valueMap.put(getColumnKeyForChoice(itemExportInfo, answer.getStringValue()),SPLIT_OPTION_SELECTED_VALUE);
        } else if (answer.getObjectValue() != null) {
            // this was a multi-select question, so we need to add all selected options
            try {
                List<String> answerValues = objectMapper.readValue(answer.getObjectValue(), new TypeReference<List<String>>() {});
                for (String answerValue : answerValues) {
                    valueMap.put(getColumnKeyForChoice(itemExportInfo, answerValue), SPLIT_OPTION_SELECTED_VALUE);
                }
            } catch (JsonProcessingException e) {
                // don't stop the entire export for one bad value, see JN-650 for aggregating these to user messages
                log.error("Error parsing answer object value enrollee: {}, question: {}, answer: {}",
                        answer.getEnrolleeId(), answer.getQuestionStableId(), answer.getId());
            }
        }
    }

    /** Returns the column key for a specific choice of a question for 'splitOptionsIntoColumns' questions */
    protected static String getColumnKeyForChoice(ItemExportInfo itemExportInfo, String choiceStableId) {
        return itemExportInfo.getBaseColumnKey() + ExportFormatUtils.COLUMN_NAME_DELIMITER + choiceStableId;
    }



    protected static String formatStringValue(String value, List<QuestionChoice> choices, boolean stableIdForOptions, Answer answer) {
        if (stableIdForOptions || choices == null || choices.isEmpty()) {
            return value;
        }
        QuestionChoice matchedChoice = choices.stream().filter(choice ->
                Objects.equals(choice.stableId(), value)).findFirst().orElse(null);
        if (matchedChoice == null) {
            log.warn("Unmatched answer option -  enrollee: {}, question: {}, answer: {}",
                    answer.getEnrolleeId(), answer.getQuestionStableId(), answer.getId());
            return value;
        }
        return matchedChoice.text();
    }

    protected static String formatObjectValue(Answer answer, List<QuestionChoice> choices, boolean stableIdForOptions, ObjectMapper objectMapper) {
        try {
            // for now, the only object values we support are arrays of strings
            String[] answerArray = objectMapper.readValue(answer.getObjectValue(), String[].class);
            if (stableIdForOptions) {
                return StringUtils.join(answerArray, ", ");
            }
            return Arrays.stream(answerArray).map(ansValue -> formatStringValue(ansValue, choices, stableIdForOptions, answer))
                    .collect(Collectors.joining(", "));
        } catch (Exception e) {
            log.warn("Error parsing answer object value enrollee: {}, question: {}, answer: {}",
                    answer.getEnrolleeId(), answer.getQuestionStableId(), answer.getId());
            return "<PARSE ERROR>";
        }
    }


}
