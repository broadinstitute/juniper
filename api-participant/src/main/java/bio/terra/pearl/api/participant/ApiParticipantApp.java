package bio.terra.pearl.api.participant;

import bio.terra.common.logging.LoggingInitializer;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(
    scanBasePackages = {
      // Scan for logging-related components & configs
      "bio.terra.common.logging",
      "bio.terra.common.iam",
      // ScanE for Liquibase migration components & configs
      "bio.terra.common.migrate",
      // Transaction management and DB retry configuration
      "bio.terra.common.retry.transaction",
      // Scan for tracing-related components & configs
      "bio.terra.common.tracing",
      "bio.terra.pearl.core",
      // Scan all service-specific packages beneath the current package
      "bio.terra.pearl.api.participant",
    })
@ConfigurationPropertiesScan("bio.terra.pearl.api.participant")
@EnableRetry
@EnableAsync(proxyTargetClass = true)
@EnableTransactionManagement
@EnableConfigurationProperties
public class ApiParticipantApp {
  public static void main(String[] args) {
    new SpringApplicationBuilder(ApiParticipantApp.class)
        .initializers(new LoggingInitializer())
        .run(args);
  }

  private final DataSource dataSource;

  public ApiParticipantApp(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  // This bean plus the @EnableTransactionManagement annotation above enables the use of the
  // @Transaction annotation to control the transaction properties of the data source.
  @Bean("transactionManager")
  public PlatformTransactionManager getTransactionManager() {
    return new JdbcTransactionManager(this.dataSource);
  }
}
