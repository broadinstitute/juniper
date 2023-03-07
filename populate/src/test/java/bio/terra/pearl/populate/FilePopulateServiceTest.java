package bio.terra.pearl.populate;


import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import bio.terra.pearl.populate.service.FilePopulateService;
import java.io.FileNotFoundException;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class FilePopulateServiceTest extends BaseSpringBootTest {
    @Autowired
    private FilePopulateService filePopulateService;

    @Test
    public void testPopulateRestrictedToSeedDirectory() throws IOException {
        FilePopulateContext config = new FilePopulateContext("/etc/passwd");
        Exception e = assertThrows(FileNotFoundException.class, () -> {
            filePopulateService.readFile("", config);
        });
    }

    @Test
    public void testPopulateDisallowsBackNavigationInBasePath() throws IOException {
        FilePopulateContext config = new FilePopulateContext("../../../etc/passwd");
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            filePopulateService.readFile("", config);
        });
    }

    @Test
    public void testPopulateDisallowsBackNavigationInRelativePath() throws IOException {
        FilePopulateContext config = new FilePopulateContext("portals/ourhealth");
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            filePopulateService.readFile("../../../../../etc/passwd", config);
        });
    }


}
