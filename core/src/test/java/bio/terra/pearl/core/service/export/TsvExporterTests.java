package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.model.survey.QuestionChoice;
import bio.terra.pearl.core.service.export.formatters.ProfileFormatter;
import bio.terra.pearl.core.service.export.formatters.module.SurveyFormatter;
import bio.terra.pearl.core.service.export.formatters.item.ItemFormatter;
import bio.terra.pearl.core.service.export.instance.ModuleExportInfo;
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
        var profileModuleInfo = ModuleExportInfo.builder()
                .moduleName("test1")
                .formatter(new ProfileFormatter()) // use the profile formatter since it's just a basic bean format
                .items(List.of(
                        ItemFormatter.builder().baseColumnKey("test1.field1").propertyAccessor("field1").build(),
                        ItemFormatter.builder().baseColumnKey("test1.field2").propertyAccessor("field2").build()
                        )).build();
        Map<String, String> valueMap = Map.of("test1.field1", "blahblah",
                "test1.field2", "bloblob");
        TsvExporter exporter = new TsvExporter(List.of(profileModuleInfo), List.of(valueMap));
        String outString = getExportResult(List.of(valueMap), List.of(profileModuleInfo));
        assertThat(outString, equalTo("test1.field1\ttest1.field2\nField 1\tField 2\nblahblah\tbloblob\n"));
    }

    @Test
    public void testSplitColumnsExport() throws Exception {
        var profileModuleInfo = ModuleExportInfo.builder()
                .moduleName("test1")
                .formatter(new SurveyFormatter(objectMapper)) // use the profile formatter since it's just a basic bean format
                .items(List.of(
                        ItemFormatter.builder()
                                .baseColumnKey("survey.q1")
                                .allowMultiple(true)
                                .splitOptionsIntoColumns(true)
                                .questionStableId("q1")
                                .choices(List.of(
                                        new QuestionChoice("choice1","Choice 1"),
                                        new QuestionChoice("choice2","Choice 2"),
                                        new QuestionChoice("choice3","Choice 3")
                                ))
                                .build()
                )).build();
        Map<String, String> valueMap = Map.of("survey.q1.choice1", "1",
                "survey.q1.choice3", "1");
        String outString = getExportResult(List.of(valueMap), List.of(profileModuleInfo));
        assertThat(outString, equalTo("test1.q1.choice1\ttest1.q1.choice2\ttest1.q1.choice3\nChoice 1\tChoice 2\tChoice 3\n1\t0\t1\n"));
    }

    private String getExportResult(List<Map<String, String>> valueMaps, List<ModuleExportInfo> moduleExportInfos) throws IOException {
        TsvExporter exporter = new TsvExporter(moduleExportInfos, valueMaps);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        exporter.export(baos);
        baos.close();
        return baos.toString();
    }
}
