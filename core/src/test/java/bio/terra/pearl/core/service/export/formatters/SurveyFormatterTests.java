package bio.terra.pearl.core.service.export.formatters;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyQuestionDefinition;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.ExportOptions;
import bio.terra.pearl.core.service.export.formatters.item.AnswerItemFormatter;
import bio.terra.pearl.core.service.export.formatters.module.ModuleFormatter;
import bio.terra.pearl.core.service.export.formatters.module.SurveyFormatter;
import bio.terra.pearl.core.service.export.formatters.item.ItemFormatter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SurveyFormatterTests extends BaseSpringBootTest {
    @Autowired
    ObjectMapper objectMapper;
    @Test
    public void testToStringMap() {
        Survey testSurvey =  Survey.builder().id(UUID.randomUUID()).stableId("oh_surveyA").version(1).build();
        SurveyQuestionDefinition questionDef = SurveyQuestionDefinition.builder()
                .questionStableId("oh_surveyA_q1")
                .questionType("text")
                .exportOrder(1)
                .build();
        SurveyFormatter moduleFormatter = new SurveyFormatter(new ExportOptions(), "oh_surveyA", List.of(testSurvey), List.of(questionDef), objectMapper);
        SurveyResponse testResponse = SurveyResponse.builder()
                .id(UUID.randomUUID())
                .surveyId(testSurvey.getId()).build();
        Answer answer = Answer.builder()
                .surveyStableId(testSurvey.getStableId())
                .questionStableId("oh_surveyA_q1")
                .surveyResponseId(testResponse.getId())
                .stringValue("easyValue")
                .build();
        EnrolleeExportData enrolleeExportData = new EnrolleeExportData(null, null,
                List.of(answer), null, List.of(testResponse), null);
        Map<String, String> valueMap = moduleFormatter.toStringMap(enrolleeExportData);

        assertThat(valueMap.get("oh_surveyA.oh_surveyA_q1"), equalTo("easyValue"));
        assertThat(valueMap.get("oh_surveyA.complete"), equalTo("false"));
    }

    @Test
    public void testAddAnswerToMapHandlesMissingVersion() throws Exception {
        SurveyQuestionDefinition questionDef = SurveyQuestionDefinition.builder()
                .questionStableId("oh_surveyA_q1")
                .questionType("text")
                .exportOrder(1)
                .build();
        Answer answerToMissingSurvey = Answer.builder()
                        .questionStableId("oh_surveyA_q1")
                        .surveyStableId("oh_surveyA")
                        .surveyVersion(18)
                        .stringValue("test123").build();
        Map<String, String> valueMap = generateAnswerMap(questionDef, answerToMissingSurvey, new ExportOptions());
        assertThat(valueMap.get("oh_surveyA.oh_surveyA_q1"), equalTo("test123"));
    }

    @Test
    public void testAddAnswerToMapStableIdForOption() throws Exception {
        SurveyQuestionDefinition questionDef = SurveyQuestionDefinition.builder()
                .questionStableId("oh_surveyA_q1")
                .questionType("radiogroup")
                .exportOrder(1)
                .allowMultiple(false)
                .choices("""
                    [{"stableId":"red","text":"Red label"},{"stableId":"blue","text":"Blue label"}]
                    """)
                .build();
        Answer singleSelect = Answer.builder()
                .questionStableId("oh_surveyA_q1")
                .surveyStableId("oh_surveyA")
                .surveyVersion(1)
                .stringValue("red").build();
        ExportOptions exportOptions = ExportOptions.builder().stableIdsForOptions(true).build();
        Map<String, String> valueMap = generateAnswerMap(questionDef, singleSelect, exportOptions);
        assertThat(valueMap.get("oh_surveyA.oh_surveyA_q1"), equalTo("red"));

        exportOptions = ExportOptions.builder().stableIdsForOptions(false).build();
        valueMap = generateAnswerMap(questionDef, singleSelect, exportOptions);
        assertThat(valueMap.get("oh_surveyA.oh_surveyA_q1"), equalTo("Red label"));
    }

    @Test
    public void testAddAnswerToMapMultiselectBothSelected() throws Exception {
        SurveyQuestionDefinition questionDef = SurveyQuestionDefinition.builder()
                .questionStableId("oh_surveyA_q1")
                .questionType("checkbox")
                .exportOrder(1)
                .allowMultiple(true)
                .choices("""
                    [{"stableId":"red","text":"Red label"},{"stableId":"blue","text":"Blue label"}]
                    """)
                .build();
        Answer multiselectBoth = Answer.builder()
                .questionStableId("oh_surveyA_q1")
                .surveyStableId("oh_surveyA")
                .surveyVersion(1)
                .objectValue("""
                        ["red", "blue"]
                        """).build();
        ExportOptions exportOptions = ExportOptions.builder().splitOptionsIntoColumns(true).build();
        Map<String, String> valueMap = generateAnswerMap(questionDef, multiselectBoth, exportOptions);
        assertThat(valueMap.get("oh_surveyA.oh_surveyA_q1.red"), equalTo("1"));
        assertThat(valueMap.get("oh_surveyA.oh_surveyA_q1.blue"), equalTo("1"));

        exportOptions = ExportOptions.builder().splitOptionsIntoColumns(false).build();
        valueMap = generateAnswerMap(questionDef, multiselectBoth, exportOptions);
        assertThat(valueMap.get("oh_surveyA.oh_surveyA_q1"), equalTo("Red label, Blue label"));

        exportOptions = ExportOptions.builder().splitOptionsIntoColumns(false).stableIdsForOptions(true).build();
        valueMap = generateAnswerMap(questionDef, multiselectBoth, exportOptions);
        assertThat(valueMap.get("oh_surveyA.oh_surveyA_q1"), equalTo("red, blue"));
    }

    @Test
    public void testAddAnswerToMapOneSelected() throws Exception {
        SurveyQuestionDefinition questionDef = SurveyQuestionDefinition.builder()
                .questionStableId("oh_surveyA_q1")
                .questionType("checkbox")
                .exportOrder(1)
                .allowMultiple(true)
                .choices("""
                    [{"stableId":"red","text":"Red label"},{"stableId":"blue","text":"Blue label"}]
                    """)
                .build();
        Answer multiselectBoth = Answer.builder()
                .questionStableId("oh_surveyA_q1")
                .surveyStableId("oh_surveyA")
                .surveyVersion(1)
                .objectValue("""
                        ["red"]
                        """).build();
        ExportOptions exportOptions = ExportOptions.builder().splitOptionsIntoColumns(true).build();
        Map<String, String> valueMap = generateAnswerMap(questionDef, multiselectBoth, exportOptions);
        assertThat(valueMap.get("oh_surveyA.oh_surveyA_q1.red"), equalTo("1"));
        assertThat(valueMap.get("oh_surveyA.oh_surveyA_q1.blue"), equalTo(null));

        exportOptions = ExportOptions.builder().splitOptionsIntoColumns(false).build();
        valueMap = generateAnswerMap(questionDef, multiselectBoth, exportOptions);
        assertThat(valueMap.get("oh_surveyA.oh_surveyA_q1"), equalTo("Red label"));

        exportOptions = ExportOptions.builder().splitOptionsIntoColumns(false).stableIdsForOptions(true).build();
        valueMap = generateAnswerMap(questionDef, multiselectBoth, exportOptions);
        assertThat(valueMap.get("oh_surveyA.oh_surveyA_q1"), equalTo("red"));
    }

    @Test
    public void testStripStudyAndSurveyPrefixes() {
        assertThat(SurveyFormatter.stripStudyAndSurveyPrefixes("oh_oh_famHx_someQuestion"), equalTo("someQuestion"));
        assertThat(SurveyFormatter.stripStudyAndSurveyPrefixes("oh_oh_famHx_someQuestion_suffix"), equalTo("someQuestion_suffix"));
        assertThat(SurveyFormatter.stripStudyAndSurveyPrefixes("nonStandardPrefix_someQuestion"), equalTo("nonStandardPrefix_someQuestion"));
        assertThat(SurveyFormatter.stripStudyAndSurveyPrefixes("someQuestion"), equalTo("someQuestion"));
    }

    /** helper for testing generation of answer maps values for a single question-answer pair */
    private Map<String, String> generateAnswerMap(SurveyQuestionDefinition question, Answer answer, ExportOptions exportOptions) throws JsonProcessingException {
        Map<String, String> valueMap = new HashMap<>();
        Survey testSurvey =  Survey.builder()
                .id(UUID.randomUUID())
                .stableId("oh_surveyA")
                .version(1)
                .build();
        SurveyFormatter moduleFormatter = new SurveyFormatter(exportOptions, "oh_surveyA", List.of(testSurvey), List.of(question), objectMapper);
        AnswerItemFormatter itemFormatter = (AnswerItemFormatter) moduleFormatter.getItemFormatters().stream().filter(
                itemInfo -> itemInfo instanceof AnswerItemFormatter)
                .findFirst().orElseThrow(() -> new IllegalStateException("formatter did not produce an AnswerItemFormatter"));
        Map<String, List<Answer>> answerMap = Map.of(question.getQuestionStableId(), List.of(answer));
        moduleFormatter.addAnswersToMap(itemFormatter, answerMap, valueMap);
        return valueMap;
    }

}
