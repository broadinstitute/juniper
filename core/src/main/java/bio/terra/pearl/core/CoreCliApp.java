package bio.terra.pearl.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Placeholder application to make spring and liquibase configuration and testing easier.
 * Running this application should trigger liquibase migrations
 */
@SpringBootApplication
@Slf4j
public class CoreCliApp
        implements CommandLineRunner {

    public static void main(String[] args) {
        log.info("STARTING APPLICATION - pearl core");
        SpringApplication.run(CoreCliApp.class, args);
        log.info("APPLICATION FINISHED - pearl core");
    }

    @Override
    public void run(String... args) {
        // do nothing
    }
}
