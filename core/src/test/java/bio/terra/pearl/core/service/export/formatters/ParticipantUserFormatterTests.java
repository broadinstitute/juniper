package bio.terra.pearl.core.service.export.formatters;

import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.formatters.module.ParticipantUserFormatter;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ParticipantUserFormatterTests {
    @Test
    public void testToStringMap() {
        ParticipantUser participantUser = ParticipantUser.builder()
                .username("tester-" + RandomStringUtils.randomAlphabetic(5))
                .createdAt(Instant.parse("2023-08-21T05:17:25.00Z"))
                .build();
        ParticipantUserFormatter moduleFormatter = new ParticipantUserFormatter(new ExportOptions());
        EnrolleeExportData exportData = new EnrolleeExportData(null, null, participantUser, null, null, null, null, null, null, null, null);
        Map<String, String> valueMap = moduleFormatter.toStringMap(exportData);

        assertThat(valueMap.get("account.username"), equalTo(participantUser.getUsername()));
        assertThat(valueMap.get("account.createdAt"), equalTo("2023-08-21 05:17AM"));
    }

    @Test
    public void testFromStringMap() {
        Map<String, String> valueMap = Map.of(
                "account.username", "tester-" + RandomStringUtils.randomAlphabetic(5),
                "account.createdAt", "2023-08-21 05:17AM"
        );
        ParticipantUserFormatter moduleFormatter = new ParticipantUserFormatter(new ExportOptions());
        ParticipantUser user = moduleFormatter.fromStringMap(UUID.randomUUID(), valueMap);
        assertThat(user.getUsername(), equalTo(valueMap.get("account.username")));
        assertThat(ExportFormatUtils.formatInstant(user.getCreatedAt()),
                equalTo(valueMap.get("account.createdAt")));
    }
}
