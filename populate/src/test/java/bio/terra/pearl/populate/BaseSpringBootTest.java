package bio.terra.pearl.populate;

import org.junit.jupiter.api.TestInfo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"test", "human-readable-logging"})
public abstract class BaseSpringBootTest {
    /**
     * convenience for getting the simple method name of a test from its TestInfo arg
     */
    protected String getTestName(TestInfo testInfo) {
        return testInfo.getTestMethod().get().getName();
    }

}
