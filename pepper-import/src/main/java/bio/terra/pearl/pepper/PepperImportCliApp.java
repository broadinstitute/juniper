package bio.terra.pearl.pepper;

import java.io.IOException;
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
    log.info("STARTING APPLICATION - pepper import cli");
    SpringApplication.run(PepperImportCliApp.class, args);
    log.info("APPLICATION FINISHED - pepper import cli");
  }

  @Override
  public void run(String... args) throws IOException {
    log.info("EXECUTING : command line importer");
  }
}
