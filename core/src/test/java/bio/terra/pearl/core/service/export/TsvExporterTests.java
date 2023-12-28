package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.survey.QuestionChoice;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyQuestionDefinition;
import bio.terra.pearl.core.service.export.formatters.item.AnswerItemFormatter;
import bio.terra.pearl.core.service.export.formatters.item.PropertyItemFormatter;
import bio.terra.pearl.core.service.export.formatters.module.EnrolleeFormatter;
import bio.terra.pearl.core.service.export.formatters.module.ModuleFormatter;
import bio.terra.pearl.core.service.export.formatters.module.ProfileFormatter;
import bio.terra.pearl.core.service.export.formatters.module.SurveyFormatter;
import bio.terra.pearl.core.service.export.formatters.item.ItemFormatter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TsvExporterTests extends BaseSpringBootTest {
    @Autowired
    ObjectMapper objectMapper;
    @Test
    public void testBasicExport() throws Exception {
        var sampleFormatter = new EnrolleeFormatter(new ExportOptions());
        // replace the formatters with a simple set we control
        sampleFormatter.getItemFormatters().clear();
        sampleFormatter.getItemFormatters().add(new PropertyItemFormatter<Enrollee>("shortcode", Enrollee.class));
        sampleFormatter.getItemFormatters().add(new PropertyItemFormatter<Enrollee>("consented", Enrollee.class));
        Map<String, String> valueMap = Map.of("enrollee.shortcode", "ABCDEF",
                "enrollee.consented", "false");
        TsvExporter exporter = new TsvExporter(List.of(sampleFormatter), List.of(valueMap));
        String outString = getExportResult(List.of(valueMap), List.of(sampleFormatter));
        assertThat(outString, equalTo("enrollee.shortcode\tenrollee.consented\nShortcode\tConsented\nABCDEF\tfalse\n"));
    }

    @Test
    public void testSplitColumnsExport() throws Exception {
        ExportOptions opts = ExportOptions.builder().splitOptionsIntoColumns(true).build();
        var surveyFormatter = new SurveyFormatter(opts, "survey", List.of(Survey.builder().stableId("survey").build()), List.of(), objectMapper);
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
