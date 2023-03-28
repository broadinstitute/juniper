package bio.terra.pearl.core.model.publishing;

import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.service.publishing.PublishingService;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.Test;

public class ConfigChangeRecordTests {
    @Test
    public void testSourceDestConstructor() throws Exception {
        PortalEnvironmentConfig sourceConfig = PortalEnvironmentConfig.builder()
                .emailSourceAddress("foo@blah.com").build();
        var destConfig = PortalEnvironmentConfig.builder()
                .emailSourceAddress("blah@blah.com").build();
        var changeRecord = new ConfigChangeRecord(sourceConfig, destConfig, "emailSourceAddress");
        assertThat(changeRecord.newValue(), equalTo("foo@blah.com"));
        assertThat(changeRecord.oldValue(), equalTo("blah@blah.com"));
    }

    @Test
    public void testHandlesNullSource() throws Exception {
        PortalEnvironmentConfig sourceConfig = null;
        var destConfig = PortalEnvironmentConfig.builder()
                .emailSourceAddress("blah@blah.com").build();
        var changeRecord = new ConfigChangeRecord(sourceConfig, destConfig, "emailSourceAddress");
        assertThat(changeRecord.newValue(), equalTo(null));
        assertThat(changeRecord.oldValue(), equalTo("blah@blah.com"));
    }

    @Test
    public void testHandlesNullDest() throws Exception {
        var sourceConfig = PortalEnvironmentConfig.builder()
                .emailSourceAddress("blah@blah.com").build();;
        PortalEnvironmentConfig destConfig = null;
        var changeRecord = new ConfigChangeRecord(sourceConfig, destConfig, "emailSourceAddress");
        assertThat(changeRecord.oldValue(), equalTo(null));
        assertThat(changeRecord.newValue(), equalTo("blah@blah.com"));
    }

    @Test
    public void testAllChanges() throws Exception {
        var sourceConfig = PortalEnvironmentConfig.builder()
                .acceptingRegistration(true)
                .participantHostname("foo")
                .emailSourceAddress("blah@blah.com").build();;
        PortalEnvironmentConfig destConfig = PortalEnvironmentConfig.builder()
                .acceptingRegistration(false)
                .participantHostname("bar")
                .emailSourceAddress("blah@blah.com").build();
        var changeRecords = ConfigChangeRecord.allChanges(sourceConfig, destConfig,
                PublishingService.BASE_IGNORE_PROPS);
        assertThat(changeRecords, hasSize(2));
        assertThat(changeRecords, hasItems(
                new ConfigChangeRecord("acceptingRegistration", false, true),
                new ConfigChangeRecord("participantHostname", (Object) "bar", (Object) "foo")
        ));
    }

    @Test
    public void testAllChangesHandlesNullSource() throws Exception {
        PortalEnvironmentConfig sourceConfig = null;;
        PortalEnvironmentConfig destConfig = PortalEnvironmentConfig.builder()
                .acceptingRegistration(false)
                .participantHostname("bar")
                .emailSourceAddress("blah@blah.com").build();
        var changeRecords = ConfigChangeRecord.allChanges(sourceConfig, destConfig,
                PublishingService.BASE_IGNORE_PROPS);
        assertThat(changeRecords, hasSize(6));
        assertThat(changeRecords, hasItems(
                new ConfigChangeRecord("emailSourceAddress", "blah@blah.com", (Object) null),
                new ConfigChangeRecord("acceptingRegistration", false, (Object) null),
                new ConfigChangeRecord("participantHostname", (Object) "bar", (Object) null),
                new ConfigChangeRecord("initialized", false, (Object) null),
                new ConfigChangeRecord("password", (Object) "broad_institute", (Object) null),
                new ConfigChangeRecord("passwordProtected", true, (Object) null)
        ));
    }

    @Test
    public void testAllChangesHandlesNullDest() throws Exception {
        PortalEnvironmentConfig destConfig = null;;
        PortalEnvironmentConfig sourceConfig = PortalEnvironmentConfig.builder()
                .acceptingRegistration(false)
                .participantHostname("bar")
                .emailSourceAddress("blah@blah.com").build();
        var changeRecords = ConfigChangeRecord.allChanges(sourceConfig, destConfig,
                PublishingService.BASE_IGNORE_PROPS);
        assertThat(changeRecords, hasSize(6));
        assertThat(changeRecords, hasItems(
                new ConfigChangeRecord("emailSourceAddress", (Object) null, (Object) "blah@blah.com"),
                new ConfigChangeRecord("acceptingRegistration", (Object) null, false),
                new ConfigChangeRecord("participantHostname", (Object) null, (Object) "bar"),
                new ConfigChangeRecord("initialized", (Object) null, false),
                new ConfigChangeRecord("password", (Object) null, (Object) "broad_institute"),
                new ConfigChangeRecord("passwordProtected", (Object) null, true)
        ));
    }

    @Test
    public void testAllChangesHandlesDoubleNull() throws Exception {
        PortalEnvironmentConfig sourceConfig = null;;
        PortalEnvironmentConfig destConfig = null;
        var changeRecords = ConfigChangeRecord.allChanges(sourceConfig, destConfig,
                PublishingService.BASE_IGNORE_PROPS);
        assertThat(changeRecords, hasSize(0));
    }
}
