package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.service.export.formatters.ProfileFormatter;
import bio.terra.pearl.core.service.export.instance.ItemExportInfo;
import bio.terra.pearl.core.service.export.instance.ModuleExportInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TsvExporterTests {
    @Autowired
    ObjectMapper objectMapper;
    @Test
    public void testBasicExport() throws Exception {
        var profileModuleInfo = ModuleExportInfo.builder()
                .moduleName("test1")
                .formatter(new ProfileFormatter()) // use the profile formatter since it's just a basic bean format
                .items(List.of(
                        ItemExportInfo.builder().baseColumnKey("test1.field1").propertyAccessor("field1").build(),
                        ItemExportInfo.builder().baseColumnKey("test1.field2").propertyAccessor("field2").build()
                        )).build();
        Map<String, String> valueMap = Map.of("test1.field1", "blahblah",
                "test1.field2", "bloblob");
        TsvExporter exporter = new TsvExporter(List.of(profileModuleInfo), List.of(valueMap));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        exporter.export(baos);
        baos.close();
        String outString = baos.toString();
        assertThat(outString, equalTo("test1.field1\ttest1.field2\nField 1\tField 2\nblahblah\tbloblob\n"));
    }
}
