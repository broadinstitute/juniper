package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.service.export.formatters.EnrolleeFormatter;
import bio.terra.pearl.core.service.export.instance.ExportOptions;
import bio.terra.pearl.core.service.export.instance.ModuleExportInfo;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class EnrolleeFormatterTests {
    @Test
    public void testToStringMap() throws Exception {
        Enrollee enrollee = Enrollee.builder()
                .shortcode("TESTER")
                .consented(true)
                .createdAt(Instant.parse("2023-08-21T05:17:25.00Z"))
                .build();
        ModuleExportInfo moduleExportInfo = new EnrolleeFormatter().getModuleExportInfo(new ExportOptions());
        EnrolleeExportData exportData = new EnrolleeExportData(enrollee, null, null, null, null);
        Map<String, String> valueMap = moduleExportInfo.toStringMap(exportData);

        assertThat(valueMap.get("enrollee.shortcode"), equalTo("TESTER"));
        assertThat(valueMap.get("enrollee.consented"), equalTo("true"));
        assertThat(valueMap.get("enrollee.createdAt"), equalTo("2023-08-21 05:17AM"));
    }
}
