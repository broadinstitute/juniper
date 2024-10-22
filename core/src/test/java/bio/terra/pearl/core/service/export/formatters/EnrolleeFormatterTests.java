package bio.terra.pearl.core.service.export.formatters;

import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.formatters.module.EnrolleeFormatter;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

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
        EnrolleeFormatter moduleFormatter = new EnrolleeFormatter(new ExportOptions());
        EnrolleeExportData exportData = new EnrolleeExportData(null, enrollee, null, null, null, null, null, null, null, null, null);
        Map<String, String> valueMap = moduleFormatter.toStringMap(exportData);

        assertThat(valueMap.get("enrollee.shortcode"), equalTo("TESTER"));
        assertThat(valueMap.get("enrollee.consented"), equalTo("true"));
        assertThat(valueMap.get("enrollee.createdAt"), equalTo("2023-08-21 05:17AM"));
    }
}
