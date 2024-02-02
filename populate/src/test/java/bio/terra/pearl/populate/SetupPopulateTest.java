package bio.terra.pearl.populate;

import bio.terra.pearl.populate.service.BaseSeedPopulator;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests that we can populate a clean environment with admin users and environments
 * This also indirectly tests the PopulateDispatcher
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SetupPopulateTest extends BaseSpringBootTest {
    @Autowired
    BaseSeedPopulator baseSeedPopulator;

    @Test
    @Transactional
    public void testSetup() throws IOException {
        BaseSeedPopulator.SetupStats setupStats = baseSeedPopulator.populate("");
        Assertions.assertEquals(BaseSeedPopulator.ADMIN_USERS_TO_POPULATE.size(), setupStats.getNumAdminUsers());
        Assertions.assertEquals(BaseSeedPopulator.ENVIRONMENTS_TO_POPULATE.size(), setupStats.getNumEnvironments());
    }
}
