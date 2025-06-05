package bio.terra.pearl.core.service.export.formatters;

import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Family;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.formatters.module.FamilyFormatter;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class FamilyFormatterTests {
    @Test
    public void testToStringMap() throws Exception {
        Family family1 = Family
                .builder()
                .proband(Enrollee.builder().shortcode("HDPROBAND1").build())
                .shortcode("F_FAM1")
                .build();
        Family family2 = Family
                .builder()
                .proband(Enrollee.builder().shortcode("HDPROBAND2").build())
                .shortcode("F_FAM2")
                .build();
        FamilyFormatter familyFormatter = new FamilyFormatter(new ExportOptions());
        EnrolleeExportData exportData = new EnrolleeExportData(null, null, null, null, null, null, null, null, null, null, List.of(family1, family2), null);
        Map<String, String> enrolleeMap = familyFormatter.toStringMap(exportData);

        assertThat(enrolleeMap.size(), equalTo(4));
        assertThat(enrolleeMap.get("family.shortcode"), equalTo("F_FAM1"));
        assertThat(enrolleeMap.get("family.proband.shortcode"), equalTo("HDPROBAND1"));
        assertThat(enrolleeMap.get("family[2].shortcode"), equalTo("F_FAM2"));
        assertThat(enrolleeMap.get("family[2].proband.shortcode"), equalTo("HDPROBAND2"));
    }
}
