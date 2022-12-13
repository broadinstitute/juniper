package bio.terra.pearl.populate;

import bio.terra.pearl.populate.service.PopulateDispatcher;
import bio.terra.pearl.populate.service.Populator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

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

    /**
     * if 2 arguments are provided, will populate a specified type from a specified file
     * @param args
     *  [0] the type, should correspond (case-insensitive) to a PopulateDispatcher.PopulateType
     *  [1] the file path, e.g. portals/ourhealth/portal.json
     * @throws IOException
     */
    @Override
    public void run(String... args) throws IOException {
        LOG.info("EXECUTING : command line populator");

        if (args.length < 2) {
            LOG.info("less than two arguments provided -- no populates to execute");
            return;
        }

        String popType = args[0];
        String filePathName = args[1];
        LOG.info("confirming default environments and users exist");
        Populator setupPopulator = populateDispatcher.getPopulator("setup");
        setupPopulator.populate("");

        LOG.info("beginning populate, type: " + popType + " from file: " + filePathName );
        Populator populator = populateDispatcher.getPopulator(popType);
        populator.populate(filePathName);
    }

    @Autowired
    private PopulateDispatcher populateDispatcher;
}
