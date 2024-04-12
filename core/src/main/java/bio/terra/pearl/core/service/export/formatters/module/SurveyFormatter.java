package bio.terra.pearl.core.service.export.formatters.module;

import bio.terra.pearl.core.model.survey.*;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.ExportOptions;
import bio.terra.pearl.core.service.export.formatters.ExportFormatUtils;
import bio.terra.pearl.core.service.export.formatters.item.AnswerItemFormatter;
import bio.terra.pearl.core.service.export.formatters.item.ItemFormatter;
import bio.terra.pearl.core.service.export.formatters.item.PropertyItemFormatter;
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
public class SurveyFormatter extends ModuleFormatter<SurveyResponse, ItemFormatter<SurveyResponse>> {
    public static String OTHER_DESCRIPTION_KEY_SUFFIX = "_description";
    public static String OTHER_DESCRIPTION_HEADER = "other description";
    public static String SPLIT_OPTION_SELECTED_VALUE = "1";
    public static String SPLIT_OPTION_UNSELECTED_VALUE = "0";
    private ObjectMapper objectMapper;

    public SurveyFormatter(ExportOptions exportOptions, String stableId, List<Survey> surveys,
                           List<SurveyQuestionDefinition> questionDefs, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        itemFormatters.add(new PropertyItemFormatter("lastUpdatedAt", SurveyResponse.class));
        itemFormatters.add(new PropertyItemFormatter("complete", SurveyResponse.class));

        // group all the questions that share a stableId (i.e. different versions of the same question), and then sort them by
        // the export order of the most recent version
        Collection<List<SurveyQuestionDefinition>> questionDefsByStableId = questionDefs.stream().collect(groupingBy(
                SurveyQuestionDefinition::getQuestionStableId
        )).values().stream().sorted(Comparator.comparingInt(a -> a.get(0).getExportOrder())).toList();
        for (List<SurveyQuestionDefinition> questionVersions : questionDefsByStableId) {
            if (List.of("signaturepad", "html").contains(questionVersions.get(0).getQuestionType())) {
                continue;
            }
            itemFormatters.add(new AnswerItemFormatter(exportOptions, moduleName, questionVersions, objectMapper));
        }

        // get the most recent survey by sorting in descending version order
        Survey latestSurvey = surveys.stream().sorted(Comparator.comparingInt(Survey::getVersion).reversed()).findFirst().get();
        displayName = latestSurvey.getName();
        moduleName = stableId;
    }
    @Override
    public String getColumnKey(ItemFormatter itemFormatter, boolean isOtherDescription, QuestionChoice choice, int moduleRepeatNum) {
        return getColumnKeyChoiceStableId(itemFormatter, isOtherDescription, choice == null ? null : choice.stableId(), moduleRepeatNum);
    }

    public String getColumnKeyChoiceStableId(ItemFormatter itemFormatter, boolean isOtherDescription, String choiceStableId, int moduleRepeatNum) {
        String columnKey = super.getColumnKey(itemFormatter, isOtherDescription, null, moduleRepeatNum);
        if (isOtherDescription) {
            columnKey += OTHER_DESCRIPTION_KEY_SUFFIX;
        } else if (choiceStableId != null && ((AnswerItemFormatter) itemFormatter).isSplitOptionsIntoColumns()) {
            columnKey += ExportFormatUtils.COLUMN_NAME_DELIMITER + choiceStableId;
        }
        return columnKey;
    }

    /** this method largely mirrors "getColumnKey", but it strips out the prefixes to make the headers more readable */
    @Override
    public String getColumnHeader(ItemFormatter itemFormatter, boolean isOtherDescription, QuestionChoice choice, int moduleRepeatNum) {
        String columnHeader = super.getColumnHeader(itemFormatter, isOtherDescription, choice, moduleRepeatNum);
        if (itemFormatter instanceof AnswerItemFormatter) {
            AnswerItemFormatter answerItemFormatter = (AnswerItemFormatter) itemFormatter;
            // for now, strip the prefixes to aid in readability.  Once we have multi-source surveys, we can revisit this.
            String cleanStableId = stripStudyAndSurveyPrefixes(answerItemFormatter.getQuestionStableId());
            columnHeader =  moduleName + ExportFormatUtils.COLUMN_NAME_DELIMITER + cleanStableId;
            if (isOtherDescription) {
                columnHeader += OTHER_DESCRIPTION_KEY_SUFFIX;
            } else if (answerItemFormatter.isSplitOptionsIntoColumns() && choice != null) {
                // the null check above is because even when we are splitting options into columns, we might need to
                // get a column header for the question as a whole (e.g. for the data dictionary), in which case
                // we would call this method with a null choice
                columnHeader += ExportFormatUtils.COLUMN_NAME_DELIMITER + choice.stableId();
            }
        }
        return columnHeader;
    }

    /** returns either the question or the choice as friendly-ish text */
    @Override
    public String getColumnSubHeader(ItemFormatter itemFormatter, boolean isOtherDescription, QuestionChoice choice, int moduleRepeatNum) {
        if (itemFormatter instanceof PropertyItemFormatter) {
            return ExportFormatUtils.camelToWordCase(((PropertyItemFormatter) itemFormatter).getPropertyName());
        }
        AnswerItemFormatter answerItemFormatter = (AnswerItemFormatter) itemFormatter;
        if (answerItemFormatter.isSplitOptionsIntoColumns() && choice != null) {
            return choice.text();
        }
        if (isOtherDescription) {
            return OTHER_DESCRIPTION_HEADER;
        }
        return ExportFormatUtils.camelToWordCase(stripStudyAndSurveyPrefixes(answerItemFormatter.getQuestionStableId()));
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


    @Override
    public Map<String, String> toStringMap(EnrolleeExportData exportData) {
        Map<String, String> valueMap = new HashMap<>();
        String surveyStableId = moduleName;
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
        for (ItemFormatter itemFormatter : getItemFormatters()) {
            if (itemFormatter instanceof PropertyItemFormatter) {
                // it's a property of the SurveyResponse
                valueMap.put(getColumnKey(itemFormatter, false, null, 1),
                        ((PropertyItemFormatter) itemFormatter).getExportString(matchedResponse));
            } else {
                // it's an answer value
                addAnswersToMap((AnswerItemFormatter) itemFormatter, answerMap, valueMap);
            }
        }
        return valueMap;
    }



    public void addAnswersToMap(AnswerItemFormatter itemFormatter,
                                Map<String, List<Answer>> answerMap, Map<String, String> valueMap) {
        List<Answer> matchedAnswers = answerMap.get(itemFormatter.getQuestionStableId());
        if (matchedAnswers == null) {
            return;
        }
        // for now, we only support one answer per question, so just return the first
        Answer matchedAnswer = matchedAnswers.get(0);
        // use the ItemExport Info matching the answer version so choices get translated correctly
        AnswerItemFormatter matchedItemFormatter = itemFormatter.getVersionMap().get(matchedAnswer.getSurveyVersion());
        if (matchedItemFormatter == null) {
            // if we can't find a match (likely because we're in a demo environment and the answer refers to a version that no longer exists)
            // just use the current version
            matchedItemFormatter = itemFormatter;
        }
        addAnswerToMap(matchedItemFormatter, matchedAnswer, valueMap, objectMapper);
    }

    protected void addAnswerToMap(AnswerItemFormatter itemFormatter,
                                         Answer answer, Map<String, String> valueMap, ObjectMapper objectMapper) {
        if (itemFormatter.isSplitOptionsIntoColumns()) {
            addSplitOptionSelectionsToMap(itemFormatter, answer, valueMap, objectMapper);
        } else {
            valueMap.put(
                    getColumnKey(itemFormatter, false, null, 1),
                    valueAsString(answer, itemFormatter.getChoices(), itemFormatter.isStableIdsForOptions(), objectMapper)
            );
        }
        if (itemFormatter.isHasOtherDescription() && answer.getOtherDescription() != null) {
            valueMap.put(
                    getColumnKey(itemFormatter, true, null, 1),
                    answer.getOtherDescription()
            );
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
    protected void addSplitOptionSelectionsToMap(ItemFormatter itemFormatter,
                                                        Answer answer, Map<String, String> valueMap, ObjectMapper objectMapper) {
        if (answer.getStringValue() != null) {
            // this was a single-select question, so we only need to add the selected option
            valueMap.put(
                    getColumnKeyChoiceStableId(itemFormatter, false, answer.getStringValue(), 1),
                    SPLIT_OPTION_SELECTED_VALUE
            );
        } else if (answer.getObjectValue() != null) {
            // this was a multi-select question, so we need to add all selected options
            try {
                List<String> answerValues = objectMapper.readValue(answer.getObjectValue(), new TypeReference<List<String>>() {});
                for (String answerValue : answerValues) {
                    valueMap.put(
                            getColumnKeyChoiceStableId(itemFormatter,false, answerValue, 1),
                            SPLIT_OPTION_SELECTED_VALUE
                    );
                }
            } catch (JsonProcessingException e) {
                // don't stop the entire export for one bad value, see JN-650 for aggregating these to user messages
                log.error("Error parsing answer object value enrollee: {}, question: {}, answer: {}",
                        answer.getEnrolleeId(), answer.getQuestionStableId(), answer.getId());
            }
        }
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
            // for now, the only object values we support explicitly parsing are arrays of strings
            String[] answerArray = objectMapper.readValue(answer.getObjectValue(), String[].class);
            if (stableIdForOptions) {
                return StringUtils.join(answerArray, ", ");
            }
            return Arrays.stream(answerArray).map(ansValue -> formatStringValue(ansValue, choices, stableIdForOptions, answer))
                    .collect(Collectors.joining(", "));
        } catch (Exception e) {
            // if we don't know what to do with it, just return the raw value
            return answer.getObjectValue();
        }
    }

    @Override
    public SurveyResponse fromStringMap(UUID studyEnvironmentId,  Map<String, String> enrolleeMap) {
        SurveyResponse response = new SurveyResponse();
        for (ItemFormatter<SurveyResponse> itemFormatter : itemFormatters) {
            String columnName = getColumnKey(itemFormatter, false, null, 1);
            String stringVal = enrolleeMap.get(columnName);
            itemFormatter.importValueToBean(response, stringVal);
        }
        return response;
    }
}
