package bio.terra.pearl.core.service.export.formatters;

import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.formatters.module.KitRequestFormatter;
import bio.terra.pearl.core.service.kit.KitRequestDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class KitRequestFormatterTests {
    @Test
    public void testToStringMap() {
        List<KitType> kitTypeList = List.of(
            KitType.builder().id(UUID.randomUUID()).name("type1").build(),
            KitType.builder().id(UUID.randomUUID()).name("type2").build()
        );
        KitRequestFormatter moduleFormatter = new KitRequestFormatter(new ExportOptions());
        String sentDate = "2023-11-17T14:57:59.548Z";
        List<KitRequestDto> kitRequests = List.of(
            KitRequestDto.builder()
                .kitType(kitTypeList.get(0))
                .status(KitRequestStatus.SENT)
                .createdAt(Instant.now())
                .sentAt(Instant.parse(sentDate))
                .build(),
            KitRequestDto.builder()
                .kitType(kitTypeList.get(1))
                .status(KitRequestStatus.DEACTIVATED)
                .createdAt(Instant.now().minus(1, java.time.temporal.ChronoUnit.DAYS))
                .build()
        );
        EnrolleeExportData exportData = new EnrolleeExportData(null, null, null, null, null, null, null, kitRequests, null, null, null);
        Map<String, String> valueMap = moduleFormatter.toStringMap(exportData);

        // the older kit should be first
        assertThat(valueMap.get("sample_kit.status"), equalTo("DEACTIVATED"));
        assertThat(valueMap.get("sample_kit.kitType"), equalTo("type2"));
        assertThat(valueMap.get("sample_kit.2.status"), equalTo("SENT"));
        assertThat(valueMap.get("sample_kit.2.kitType"), equalTo("type1"));
        assertThat(valueMap.get("sample_kit.2.sentAt"), containsString("2023-11-17"));
    }
}
