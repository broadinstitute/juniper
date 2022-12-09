package bio.terra.pearl.populate;

import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.populate.service.EnvironmentPopulator;
import bio.terra.pearl.populate.service.PortalPopulator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Attempts to populate every portal in the seed path.
 * Since the aim of the seed path is to have portals sufficient for a developer/demo-er to easily view
 * all aspects of product functionality.  So this test essentially confirms that a fresh development/demo
 * environment can be created with all functionality accessible.
 */
public class PopulatePortalsTest extends BaseSpringBootTest {
    private List<String> portalFolders = Arrays.asList("ourhealth");
    private List<String> environmentPrereqs = Arrays.asList("sandbox", "irb", "live");
    @Autowired
    private PortalPopulator portalPopulator;
    @Autowired
    private EnvironmentPopulator environmentPopulator;
    @Test
    @Transactional
    public void testPopulateAll() throws IOException {
        for (String envName : environmentPrereqs) {
            environmentPopulator.populate("environments/" + envName + ".json");
        }

        for (String portalFolder : portalFolders) {
            Portal portal = portalPopulator.populate("portals/" + portalFolder +"/portal.json");
            // For now, just do a check on the shortcode to confirm we got the right one.
            // We can add more detailed assertions as our populates get more sophisticated
            Assertions.assertEquals(portalFolder, portal.getShortcode());
        }
    }
}
