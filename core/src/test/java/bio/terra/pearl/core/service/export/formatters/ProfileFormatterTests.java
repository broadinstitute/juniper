package bio.terra.pearl.core.service.export.formatters;

import bio.terra.pearl.core.model.address.MailingAddress;
import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.formatters.module.ProfileFormatter;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ProfileFormatterTests {
    @Test
    public void testToStringMap() throws Exception {
        Profile profile = Profile.builder()
                .familyName("Tester")
                .givenName("Bob")
                .birthDate(LocalDate.ofYearDay(1997, 4))
                .doNotEmail(false)
                .mailingAddress(MailingAddress.builder()
                        .city("Boston")
                        .build())
                .build();
        ProfileFormatter moduleFormatter = new ProfileFormatter(new ExportOptions());
        EnrolleeExportData exportData = new EnrolleeExportData(null, null, null, profile, null, null, null, null, null, null, null);
        Map<String, String> enrolleeMap = moduleFormatter.toStringMap(exportData);

        assertThat(enrolleeMap.get("profile.familyName"), equalTo("Tester"));
        assertThat(enrolleeMap.get("profile.givenName"), equalTo("Bob"));
        assertThat(enrolleeMap.get("profile.birthDate"), equalTo("1997-01-04"));
        assertThat(enrolleeMap.get("profile.doNotEmail"), equalTo("false"));
        assertThat(enrolleeMap.get("profile.mailingAddress.city"), equalTo("Boston"));
    }

    @Test
    public void testToStringMapWithNoAddress() throws Exception {
        Profile profile = Profile.builder()
                .familyName("Tester")
                .build();
        ProfileFormatter moduleFormatter = new ProfileFormatter(new ExportOptions());
        EnrolleeExportData exportData = new EnrolleeExportData(null, null, null, profile, null, null, null, null, null, null, null);
        Map<String, String> enrolleeMap = moduleFormatter.toStringMap(exportData);

        assertThat(enrolleeMap.get("profile.familyName"), equalTo("Tester"));
        assertThat(enrolleeMap.get("profile.mailingAddress.city"), equalTo(""));
    }
}
