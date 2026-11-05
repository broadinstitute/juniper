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
            SIDREnvironment sandbox = SIDREnvironment.valueOf("SANDBOX" + i);
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
    void urlsAndAudiencesUseTheLowercaseName() {
        // the enum is persisted by name (uppercase), but SIDR's hosts and audiences are lowercase
        assertEquals("sandbox3", SIDREnvironment.SANDBOX3.toString());
        assertEquals("SANDBOX3", SIDREnvironment.SANDBOX3.name());
    }

    @Test
    void canaryIsNotAnOption() {
        // GPO has a canary environment, but Juniper doesn't order kits there
        assertThrows(IllegalArgumentException.class, () -> SIDREnvironment.valueOf("CANARY"));
        assertNotNull(SIDREnvironment.valueOf("DEV"));
    }
}
