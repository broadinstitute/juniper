package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyQuestionDefinition;
import bio.terra.pearl.core.service.export.formatters.item.AnswerItemFormatter;
import bio.terra.pearl.core.service.export.formatters.item.PropertyItemFormatter;
import bio.terra.pearl.core.service.export.formatters.module.EnrolleeFormatter;
import bio.terra.pearl.core.service.export.formatters.module.ModuleFormatter;
import bio.terra.pearl.core.service.export.formatters.module.SurveyFormatter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class TsvExporterTests extends BaseSpringBootTest {
    @Autowired
    ObjectMapper objectMapper;
    @Test
    public void testBasicExport() throws Exception {
        EnrolleeFormatter sampleFormatter = new EnrolleeFormatter(new ExportOptions());
        // replace the formatters with a simple set we control
        sampleFormatter.getItemFormatters().clear();
        sampleFormatter.getItemFormatters().add(new PropertyItemFormatter<Enrollee>("shortcode", Enrollee.class));
        sampleFormatter.getItemFormatters().add(new PropertyItemFormatter<Enrollee>("consented", Enrollee.class));
        Map<String, String> valueMap = Map.of("enrollee.shortcode", "ABCDEF",
                "enrollee.consented", "false");
        String outString = getExportResult(List.of(valueMap), List.of(sampleFormatter));
        assertThat(outString, equalTo("enrollee.shortcode\tenrollee.consented\nShortcode\tConsented\nABCDEF\tfalse\n"));
    }

    @Test
    public void testExportValueSanitization() throws Exception {
        EnrolleeFormatter sampleFormatter = new EnrolleeFormatter(new ExportOptions());
        // replace the formatters with a simple set we control
        // replace the formatters with a simple set we control
        sampleFormatter.getItemFormatters().clear();
        sampleFormatter.getItemFormatters().add(new PropertyItemFormatter<Enrollee>("shortcode", Enrollee.class));
        sampleFormatter.getItemFormatters().add(new PropertyItemFormatter<Enrollee>("consented", Enrollee.class));
        Map<String, String> valueMap = Map.of("enrollee.shortcode", "ABCD\"EF",
                "enrollee.consented", "fa\tlse");
        String outString = getExportResult(List.of(valueMap), List.of(sampleFormatter));
        assertThat(outString, equalTo("enrollee.shortcode\tenrollee.consented\nShortcode\tConsented\n\"ABCD\"\"EF\"\t\"fa\tlse\"\n"));
    }

    @Test
    public void testExportHeaderSanitization() throws Exception {
        Survey survey = Survey.builder().stableId("survey1").build();
        SurveyQuestionDefinition questionDef = SurveyQuestionDefinition.builder()
                .questionStableId("tabTrailing\t")
                .questionText("Question 1")
                .questionType("text")
                .build();
        SurveyFormatter sampleFormatter = new SurveyFormatter(new ExportOptions(), "survey1", List.of(survey), List.of(questionDef), List.of(), objectMapper);
        Map<String, String> valueMap = Map.of("survey1.tabTrailing\t", "blah");
        String outString = getExportResult(List.of(valueMap), List.of(sampleFormatter));
        // header and subheader should be quoted
        assertThat(outString, equalTo("survey1.lastUpdatedAt\tsurvey1.complete\t\"survey1.tabTrailing\t\"\nLast Updated At\tComplete\t\"Tab Trailing \t\"\n\"\"\t\tblah\n"));
    }

    @Test
    public void testExportJson() throws Exception {
        Survey survey = Survey.builder().stableId("survey1").build();
        SurveyQuestionDefinition questionDef = SurveyQuestionDefinition.builder()
                .questionStableId("q1")
                .questionText("Question 1")
                .questionType("text")
                .build();
        SurveyFormatter sampleFormatter = new SurveyFormatter(new ExportOptions(), "survey1", List.of(survey), List.of(questionDef), List.of(), objectMapper);
        Map<String, String> valueMap = Map.of("survey1.q1", "{\"key\": \"value\"}");
        String outString = getExportResult(List.of(valueMap), List.of(sampleFormatter));
        // the extra quotes here are expected; it conforms to RFC 4180
        assertThat(outString, equalTo("survey1.lastUpdatedAt\tsurvey1.complete\tsurvey1.q1\nLast Updated At\tComplete\tQ 1\n\"\"\t\t\"{\"\"key\"\": \"\"value\"\"}\"\n"));
    }

    @Test
    public void testSplitColumnsExport() throws Exception {
        ExportOptions opts = ExportOptions.builder().splitOptionsIntoColumns(true).build();
        SurveyFormatter surveyFormatter = new SurveyFormatter(opts, "survey", List.of(Survey.builder().stableId("survey").build()), List.of(), List.of(), objectMapper);
        // replace the formatters with a simple set we control
        surveyFormatter.getItemFormatters().clear();
        surveyFormatter.getItemFormatters().add(
                new AnswerItemFormatter(opts, "survey", List.of(
                        SurveyQuestionDefinition.builder()
                                .questionStableId("q1")
                                .allowMultiple(true)
                                .choices("""
                                        [
                                          {
                                            "stableId": "choice1",
                                            "text": "Choice 1"
                                          },
                                          {
                                            "stableId": "choice2",
                                            "text": "Choice 2"
                                          },
                                          {
                                            "stableId": "choice3",
                                            "text": "Choice 3"
                                          }
                                        ]
                                        """)
                                .build()), objectMapper)
                );

        Map<String, String> valueMap = Map.of("survey.q1.choice1", "1",
                "survey.q1.choice3", "1");
        String outString = getExportResult(List.of(valueMap), List.of(surveyFormatter));
        assertThat(outString, equalTo("survey.q1.choice1\tsurvey.q1.choice2\tsurvey.q1.choice3\nChoice 1\tChoice 2\tChoice 3\n1\t0\t1\n"));
    }

    private String getExportResult(List<Map<String, String>> valueMaps, List<ModuleFormatter> moduleFormatters) throws IOException {
        TsvExporter exporter = new TsvExporter(moduleFormatters, valueMaps);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        exporter.export(baos);
        baos.close();
        return baos.toString();
    }
}
