package bio.terra.pearl.pepper;

import java.io.IOException;
import java.nio.file.Path;

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

  public static void main(String[] args) {
    SpringApplication.run(PepperImportCliApp.class, args);
  }

  @Override
  public void run(String... args) throws IOException {
    log.info("EXECUTING : command line importer");
    ActivityImporter activityImporter = new ActivityImporter(Path.of("../pepper-apis/studybuilder-cli/studies/atcp/prequal.conf"));
    activityImporter.buildActivity(Path.of("../pepper-apis/studybuilder-cli/studies/atcp/prequal.conf"));
  }
}
