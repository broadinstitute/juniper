package bio.terra.pearl.populate;

import bio.terra.pearl.populate.service.EnvironmentPopulator;
import bio.terra.pearl.populate.service.PortalPopulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

/**
 * Placeholder application to ake spring and liquibase configuration and testing easier.
 * Running this application should trigger liquibase migrations
 */
@SpringBootApplication(scanBasePackages = {"bio.terra.pearl.core", "bio.terra.pearl.populate"})
public class PopulateCliApp
        implements CommandLineRunner {

    private static Logger LOG = LoggerFactory
            .getLogger(PopulateCliApp.class);

    public static void main(String[] args) {
        LOG.info("STARTING THE APPLICATION");
        SpringApplication.run(PopulateCliApp.class, args);
        LOG.info("APPLICATION FINISHED");
    }

    @Override
    public void run(String... args) throws IOException {
        LOG.info("EXECUTING : command line runner");

        for (int i = 0; i < args.length; ++i) {
            LOG.info("args[{}]: {}", i, args[i]);
        }
        environmentPopulator.populate("environments/sandbox.json");
        environmentPopulator.populate("environments/irb.json");
        environmentPopulator.populate("environments/production.json");
        portalPopulator.populate("portals/ourhealth/portal.json");
    }

    @Autowired
    private PortalPopulator portalPopulator;

    @Autowired
    private EnvironmentPopulator environmentPopulator;
}
