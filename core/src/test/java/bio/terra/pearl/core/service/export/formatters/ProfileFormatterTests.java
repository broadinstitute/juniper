package bio.terra.pearl.core.service.export.formatters;

import bio.terra.pearl.core.model.participant.MailingAddress;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.formatters.ProfileFormatter;
import bio.terra.pearl.core.service.export.instance.ExportOptions;
import bio.terra.pearl.core.service.export.instance.ModuleExportInfo;
import java.time.LocalDate;
import java.util.Map;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Test;

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
        ModuleExportInfo moduleExportInfo = new ProfileFormatter().getModuleExportInfo(new ExportOptions());
        EnrolleeExportData exportData = new EnrolleeExportData(null, profile, null, null, null);
        Map<String, String> enrolleeMap = moduleExportInfo.toStringMap(exportData);

        assertThat(enrolleeMap.get("profile.familyName"), equalTo("Tester"));
        assertThat(enrolleeMap.get("profile.givenName"), equalTo("Bob"));
        assertThat(enrolleeMap.get("profile.birthDate"), equalTo("1997-01-04"));
        assertThat(enrolleeMap.get("profile.doNotEmail"), equalTo("false"));
        assertThat(enrolleeMap.get("profile.mailingAddress.city"), equalTo("Boston"));
    }

    public void testToStringMapWithNoAddress() throws Exception {
        Profile profile = Profile.builder()
                .familyName("Tester")
                .build();
        ModuleExportInfo moduleExportInfo = new ProfileFormatter().getModuleExportInfo(new ExportOptions());
        EnrolleeExportData exportData = new EnrolleeExportData(null, profile, null, null, null);
        Map<String, String> enrolleeMap = moduleExportInfo.toStringMap(exportData);

        assertThat(enrolleeMap.get("profile.familyName"), equalTo("Tester"));
        assertThat(enrolleeMap.get("profile.mailingAddress.city"), equalTo(""));
    }
}
