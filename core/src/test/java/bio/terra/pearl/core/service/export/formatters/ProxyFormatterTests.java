package bio.terra.pearl.core.service.export.formatters;

import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.formatters.module.ProxyFormatter;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ProxyFormatterTests {
    @Test
    public void testToStringMap() throws Exception {
        ParticipantUser user1 = ParticipantUser
                .builder()
                .username("proxy1@test.com")
                .build();
        ParticipantUser user2 = ParticipantUser
                .builder()
                .username("proxy2@test.com")
                .build();
        ProxyFormatter proxyFormatter = new ProxyFormatter(new ExportOptions());
        EnrolleeExportData exportData = new EnrolleeExportData(null, null, null, null, null, null, null, null, null, null, List.of(user1, user2));
        Map<String, String> enrolleeMap = proxyFormatter.toStringMap(exportData);

        assertThat(enrolleeMap.size(), equalTo(2));
        System.out.println(enrolleeMap.keySet());
        assertThat(enrolleeMap.get("proxy.username"), equalTo("proxy1@test.com"));
        assertThat(enrolleeMap.get("proxy.2.username"), equalTo("proxy2@test.com"));

    }
}
