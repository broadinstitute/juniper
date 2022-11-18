package bio.terra.pearl.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Placeholder application to ake spring and liquibase configuration and testing easier.
 * Running this application should trigger liquibase migrations
 */
@SpringBootApplication
public class CliApp
        implements CommandLineRunner {

    private static Logger LOG = LoggerFactory
            .getLogger(CliApp.class);

    public static void main(String[] args) {
        LOG.info("STARTING THE APPLICATION");
        SpringApplication.run(CliApp.class, args);
        LOG.info("APPLICATION FINISHED");
    }

    @Override
    public void run(String... args) {
        LOG.info("EXECUTING : command line runner");

        for (int i = 0; i < args.length; ++i) {
            LOG.info("args[{}]: {}", i, args[i]);
        }
    }
}
