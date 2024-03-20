package bio.terra.pearl.pepper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CLI populator -- see the "run" method for argument descriptions. this will no-op if no arguments
 * are passed
 */
@SpringBootApplication(scanBasePackages = {"bio.terra.pearl.core", "bio.terra.pearl.populate"})
@Slf4j
public class PepperImportCliApp implements CommandLineRunner {

  public static final String ABSOLUTE_SEED_ROOT = "pepper-import/src/main/resources/pepper";

  public static void main(String[] args) {
    SpringApplication.run(PepperImportCliApp.class, args);
  }

  @Override
  public void run(String... args) throws IOException {
    log.info("EXECUTING : command line importer");
    Config varsCfg = ConfigFactory.parseFile(getFilePath("studies/atcp/substitutions.conf").toFile());
    ActivityImporter activityImporter = new ActivityImporter(getFilePath("studies/atcp"), varsCfg);

    activityImporter.buildActivity(Path.of("prequal.conf"));
  }

  public static Path getFilePath(String path) {
    /**
     * depending on whether you are running gradle or spring boot, the root directory could either be
     * the root folder or pepper-import.  So strip out api-admin or populate if it's there
     */
    String projectDir = System.getProperty("user.dir").replace("/pepper-import", "");
    String pathName = projectDir + "/" + ABSOLUTE_SEED_ROOT + "/" + path;
    return Path.of(pathName);
  }
}
