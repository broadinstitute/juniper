package bio.terra.pearl.core.model.publishing;

import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.service.publishing.PortalDiffService;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ConfigChangeTests {
    @Test
    public void testSourceDestConstructor() throws Exception {
        PortalEnvironmentConfig sourceConfig = PortalEnvironmentConfig.builder()
                .emailSourceAddress("foo@blah.com").build();
        PortalEnvironmentConfig destConfig = PortalEnvironmentConfig.builder()
                .emailSourceAddress("blah@blah.com").build();
        ConfigChange changeRecord = new ConfigChange(sourceConfig, destConfig, "emailSourceAddress");
        assertThat(changeRecord.newValue(), equalTo("foo@blah.com"));
        assertThat(changeRecord.oldValue(), equalTo("blah@blah.com"));
    }

    @Test
    public void testHandlesNullSource() throws Exception {
        PortalEnvironmentConfig sourceConfig = null;
        PortalEnvironmentConfig destConfig = PortalEnvironmentConfig.builder()
                .emailSourceAddress("blah@blah.com").build();
        ConfigChange changeRecord = new ConfigChange(sourceConfig, destConfig, "emailSourceAddress");
        assertThat(changeRecord.newValue(), equalTo(null));
        assertThat(changeRecord.oldValue(), equalTo("blah@blah.com"));
    }

    @Test
    public void testHandlesNullDest() throws Exception {
        PortalEnvironmentConfig sourceConfig = PortalEnvironmentConfig.builder()
                .emailSourceAddress("blah@blah.com").build();;
        PortalEnvironmentConfig destConfig = null;
        ConfigChange changeRecord = new ConfigChange(sourceConfig, destConfig, "emailSourceAddress");
        assertThat(changeRecord.oldValue(), equalTo(null));
        assertThat(changeRecord.newValue(), equalTo("blah@blah.com"));
    }

    @Test
    public void testAllChanges() throws Exception {
        PortalEnvironmentConfig sourceConfig = PortalEnvironmentConfig.builder()
                .acceptingRegistration(true)
                .participantHostname("foo")
                .emailSourceAddress("blah@blah.com").build();;
        PortalEnvironmentConfig destConfig = PortalEnvironmentConfig.builder()
                .acceptingRegistration(false)
                .participantHostname("bar")
                .emailSourceAddress("blah@blah.com").build();
        List<ConfigChange> changeRecords = ConfigChange.allChanges(sourceConfig, destConfig,
                PortalDiffService.CONFIG_IGNORE_PROPS);
        assertThat(changeRecords, hasSize(2));
        assertThat(changeRecords, hasItems(
                new ConfigChange("acceptingRegistration", false, true),
                new ConfigChange("participantHostname", (Object) "bar", (Object) "foo")
        ));
    }

    @Test
    public void testAllChangesHandlesNullSource() throws Exception {
        PortalEnvironmentConfig sourceConfig = null;;
        PortalEnvironmentConfig destConfig = PortalEnvironmentConfig.builder()
                .acceptingRegistration(false)
                .participantHostname("bar")
                .emailSourceAddress("blah@blah.com").build();
        List<ConfigChange> changeRecords = ConfigChange.allChanges(sourceConfig, destConfig,
                PortalDiffService.CONFIG_IGNORE_PROPS);
        assertThat(changeRecords, hasSize(7));
        assertThat(changeRecords, hasItems(
                new ConfigChange("emailSourceAddress", "blah@blah.com", (Object) null),
                new ConfigChange("acceptingRegistration", false, (Object) null),
                new ConfigChange("participantHostname", (Object) "bar", (Object) null),
                new ConfigChange("initialized", false, (Object) null),
                new ConfigChange("password", (Object) "broad_institute", (Object) null),
                new ConfigChange("passwordProtected", true, (Object) null),
                new ConfigChange("defaultLanguage", "en", (Object) null)
        ));
    }

    @Test
    public void testAllChangesHandlesNullDest() throws Exception {
        PortalEnvironmentConfig destConfig = null;;
        PortalEnvironmentConfig sourceConfig = PortalEnvironmentConfig.builder()
                .acceptingRegistration(false)
                .participantHostname("bar")
                .emailSourceAddress("blah@blah.com").build();
        List<ConfigChange> changeRecords = ConfigChange.allChanges(sourceConfig, destConfig,
                PortalDiffService.CONFIG_IGNORE_PROPS);
        assertThat(changeRecords, hasSize(7));
        assertThat(changeRecords, hasItems(
                new ConfigChange("emailSourceAddress", (Object) null, (Object) "blah@blah.com"),
                new ConfigChange("acceptingRegistration", (Object) null, false),
                new ConfigChange("participantHostname", (Object) null, (Object) "bar"),
                new ConfigChange("initialized", (Object) null, false),
                new ConfigChange("password", (Object) null, (Object) "broad_institute"),
                new ConfigChange("passwordProtected", (Object) null, true),
                new ConfigChange("defaultLanguage", (Object) null, (Object) "en")
        ));
    }

    @Test
    public void testAllChangesHandlesDoubleNull() throws Exception {
        PortalEnvironmentConfig sourceConfig = null;;
        PortalEnvironmentConfig destConfig = null;
        List<ConfigChange> changeRecords = ConfigChange.allChanges(sourceConfig, destConfig,
                PortalDiffService.CONFIG_IGNORE_PROPS);
        assertThat(changeRecords, hasSize(0));
    }
}
