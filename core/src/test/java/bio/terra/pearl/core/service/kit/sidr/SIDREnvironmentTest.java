package bio.terra.pearl.core.service.kit.sidr;

import bio.terra.pearl.core.service.kit.sidr.models.SIDREnvironment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SIDREnvironmentTest {
    @Test
    void productionHasNoEnvironmentSuffixInItsUrl() {
        assertEquals("https://gpo.broadinstitute.org", SIDREnvironment.PRODUCTION.getAPIURl());
        assertEquals("broad-gpo-production", SIDREnvironment.PRODUCTION.getAPIAudience());
    }

    @Test
    void otherEnvironmentsAreSuffixedInUrlAndAudience() {
        assertEquals("https://gpo-staging.broadinstitute.org", SIDREnvironment.STAGING.getAPIURl());
        assertEquals("broad-gpo-staging", SIDREnvironment.STAGING.getAPIAudience());
        assertEquals("https://gpo-dev.broadinstitute.org", SIDREnvironment.DEV.getAPIURl());
        assertEquals("broad-gpo-dev", SIDREnvironment.DEV.getAPIAudience());
        for (int i = 1; i <= 5; i++) {
            SIDREnvironment sandbox = SIDREnvironment.fromConfigValue("sandbox" + i);
            assertEquals("https://gpo-sandbox%d.broadinstitute.org".formatted(i), sandbox.getAPIURl());
            assertEquals("broad-gpo-sandbox" + i, sandbox.getAPIAudience());
        }
    }

    /** SIDR only accepts tokens for audiences and hosts of these shapes, so every environment must match them */
    @Test
    void allEnvironmentsMatchTheShapesSidrAccepts() {
        for (SIDREnvironment env : SIDREnvironment.values()) {
            assertTrue(env.getAPIAudience().matches("^broad-gpo-\\w+$"), env.name());
            assertTrue(env.getAPIURl().matches("https://gpo(-\\w+)?\\.broadinstitute\\.org"), env.name());
        }
    }

    @Test
    void configValuesAreParsedCaseInsensitively() {
        assertEquals(SIDREnvironment.PRODUCTION, SIDREnvironment.fromConfigValue("production"));
        assertEquals(SIDREnvironment.SANDBOX2, SIDREnvironment.fromConfigValue(" Sandbox2 "));
        assertEquals(SIDREnvironment.DEV, SIDREnvironment.fromConfigValue("dev"));
        // GPO has a canary environment too, but Juniper doesn't order kits there
        assertThrows(IllegalArgumentException.class, () -> SIDREnvironment.fromConfigValue("canary"));
        assertThrows(IllegalArgumentException.class, () -> SIDREnvironment.fromConfigValue(""));
        assertThrows(IllegalArgumentException.class, () -> SIDREnvironment.fromConfigValue(null));
    }

    @Test
    void everyEnvironmentRoundTripsThroughTheStoredConfigValue() {
        // StudyEnvironmentConfig.sidrEnvironment stores the lowercase name, which is also toString()
        for (SIDREnvironment env : SIDREnvironment.values()) {
            assertEquals(env, SIDREnvironment.fromConfigValue(env.toString()));
        }
    }
}
