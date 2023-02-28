package bio.terra.pearl.populate;


import bio.terra.pearl.populate.service.FilePopulateConfig;
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
        FilePopulateConfig config = new FilePopulateConfig("/etc/passwd");
        Exception e = assertThrows(FileNotFoundException.class, () -> {
            filePopulateService.readFile("", config);
        });
    }

    @Test
    public void testPopulateDisallowsBackNavigationInBasePath() throws IOException {
        FilePopulateConfig config = new FilePopulateConfig("../../../etc/passwd");
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            filePopulateService.readFile("", config);
        });
    }

    @Test
    public void testPopulateDisallowsBackNavigationInRelativePath() throws IOException {
        FilePopulateConfig config = new FilePopulateConfig("portals/ourhealth");
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            filePopulateService.readFile("../../../../../etc/passwd", config);
        });
    }


}
