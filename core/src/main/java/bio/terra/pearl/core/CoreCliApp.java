package bio.terra.pearl.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Placeholder application to make spring and liquibase configuration and testing easier.
 * Running this application should trigger liquibase migrations
 */
@SpringBootApplication
public class CoreCliApp
        implements CommandLineRunner {

    private static Logger LOG = LoggerFactory
            .getLogger(CoreCliApp.class);

    public static void main(String[] args) {
        LOG.info("STARTING APPLICATION - pearl core");
        SpringApplication.run(CoreCliApp.class, args);
        LOG.info("APPLICATION FINISHED - pearl core");
    }

    @Override
    public void run(String... args) {
        // do nothing
    }
}
