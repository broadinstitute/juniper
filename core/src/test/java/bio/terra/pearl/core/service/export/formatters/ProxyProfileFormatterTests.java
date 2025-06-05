package bio.terra.pearl.core.service.export.formatters;

import bio.terra.pearl.core.model.address.MailingAddress;
import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.formatters.module.ProxyProfileFormatter;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ProxyProfileFormatterTests {
    @Test
    public void testToStringMap() throws Exception {
        Profile proxy1 = Profile.builder()
                .createdAt(Instant.now())
                .familyName("First")
                .givenName("Proxy")
                .birthDate(LocalDate.ofYearDay(1997, 4))
                .doNotEmail(false)
                .mailingAddress(MailingAddress.builder()
                        .city("Boston")
                        .build())
                .build();
        Profile proxy2 = Profile.builder()
                .createdAt(Instant.now())
                .familyName("Second")
                .givenName("Proxy")
                .birthDate(LocalDate.ofYearDay(1994, 2))
                .doNotEmail(false)
                .mailingAddress(MailingAddress.builder()
                        .city("Cambridge")
                        .build())
                .build();
        ProxyProfileFormatter moduleFormatter = new ProxyProfileFormatter(new ExportOptions());
        EnrolleeExportData exportData = new EnrolleeExportData(null, null, null, null, List.of(proxy1, proxy2), null, null, null, null, null, null, null);
        Map<String, String> enrolleeMap = moduleFormatter.toStringMap(exportData);

        // gets most recent first (e.g., proxy2)
        assertThat(enrolleeMap.get("proxyProfile.familyName"), equalTo("Second"));
        assertThat(enrolleeMap.get("proxyProfile.givenName"), equalTo("Proxy"));
        assertThat(enrolleeMap.get("proxyProfile.birthDate"), equalTo("1994-01-02"));
        assertThat(enrolleeMap.get("proxyProfile.doNotEmail"), equalTo("false"));
        assertThat(enrolleeMap.get("proxyProfile.mailingAddress.city"), equalTo("Cambridge"));

        assertThat(enrolleeMap.get("proxyProfile[2].familyName"), equalTo("First"));
        assertThat(enrolleeMap.get("proxyProfile[2].givenName"), equalTo("Proxy"));
        assertThat(enrolleeMap.get("proxyProfile[2].birthDate"), equalTo("1997-01-04"));
        assertThat(enrolleeMap.get("proxyProfile[2].doNotEmail"), equalTo("false"));
        assertThat(enrolleeMap.get("proxyProfile[2].mailingAddress.city"), equalTo("Boston"));

    }

    @Test
    public void testToStringMapWithNoAddress() throws Exception {
        Profile profile = Profile.builder()
                .familyName("Tester")
                .build();
        ProxyProfileFormatter moduleFormatter = new ProxyProfileFormatter(new ExportOptions());
        EnrolleeExportData exportData = new EnrolleeExportData(null, null, null, null, List.of(profile), null, null, null, null, null, null, null);
        Map<String, String> enrolleeMap = moduleFormatter.toStringMap(exportData);

        assertThat(enrolleeMap.get("proxyProfile.familyName"), equalTo("Tester"));
        assertThat(enrolleeMap.get("proxyProfile.mailingAddress.city"), equalTo(""));
    }

    @Test
    public void testFromStringMap() {
        Map<String, String> valueMap = Map.of(
                "proxyProfile.familyName", "Tester",
                "proxyProfile.givenName", "Bob",
                "proxyProfile.birthDate", "1997-01-04",
                "proxyProfile.doNotEmail", "false",
                "proxyProfile.mailingAddress.city", "Boston"
        );
        ProxyProfileFormatter moduleFormatter = new ProxyProfileFormatter(new ExportOptions());
        Profile profile = moduleFormatter.fromStringMap(UUID.randomUUID(), valueMap, 1);

        assertThat(profile.getFamilyName(), equalTo(valueMap.get("proxyProfile.familyName")));
        assertThat(profile.getGivenName(), equalTo(valueMap.get("proxyProfile.givenName")));
        assertThat(profile.getBirthDate().toString(), equalTo(valueMap.get("proxyProfile.birthDate")));
        assertThat(profile.isDoNotEmail(), equalTo(Boolean.parseBoolean(valueMap.get("proxyProfile.doNotEmail"))));
        assertThat(profile.getMailingAddress().getCity(), equalTo(valueMap.get("proxyProfile.mailingAddress.city")));
    }
}
