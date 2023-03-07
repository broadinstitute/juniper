package bio.terra.pearl.populate;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CLI populator -- see the "run" method for argument descriptions.  this will no-op if no arguments are passed
 */
@SpringBootApplication(scanBasePackages = {"bio.terra.pearl.core", "bio.terra.pearl.populate"})
public class PopulateCliApp
        implements CommandLineRunner {

    private static Logger LOG = LoggerFactory
            .getLogger(PopulateCliApp.class);

    public static void main(String[] args) {
        LOG.info("STARTING APPLICATION - pearl populate cli");
        SpringApplication.run(PopulateCliApp.class, args);
        LOG.info("APPLICATION FINISHED - pearl populate cli");
    }

    @Override
    public void run(String... args) throws IOException {
        LOG.info("EXECUTING : command line populator");
    }
}
