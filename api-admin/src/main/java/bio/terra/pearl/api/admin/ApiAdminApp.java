package bio.terra.pearl.api.admin;

import bio.terra.common.logging.LoggingInitializer;
import javax.sql.DataSource;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(
    scanBasePackages = {
      // Scan for logging-related components & configs
      "bio.terra.common.logging",
      "bio.terra.common.iam",
      // Scan for Liquibase migration components & configs
      "bio.terra.common.migrate",
      // Transaction management and DB retry configuration
      "bio.terra.common.retry.transaction",
      // Scan for tracing-related components & configs
      "bio.terra.common.tracing",
      "bio.terra.pearl.core",
      "bio.terra.pearl.populate",
      // Scan all service-specific packages beneath the current package
      "bio.terra.pearl.api.admin",
    })
@ConfigurationPropertiesScan("bio.terra.pearl.api.admin")
@EnableRetry
@EnableTransactionManagement
@EnableAsync(proxyTargetClass = true)
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "60m")
@EnableConfigurationProperties
public class ApiAdminApp {
  public static void main(String[] args) {
    new SpringApplicationBuilder(ApiAdminApp.class)
        .initializers(new LoggingInitializer())
        .run(args);
  }

  private final DataSource dataSource;

  public ApiAdminApp(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  // This bean plus the @EnableTransactionManagement annotation above enables the use of the
  // @Transaction annotation to control the transaction properties of the data source.
  @Bean("transactionManager")
  public PlatformTransactionManager getTransactionManager() {
    return new JdbcTransactionManager(this.dataSource);
  }

  @Bean
  public LockProvider lockProvider(DataSource dataSource) {
    return new JdbcTemplateLockProvider(
        JdbcTemplateLockProvider.Configuration.builder()
            .withJdbcTemplate(new JdbcTemplate(dataSource))
            .usingDbTime() // Works on Postgres, MySQL, MariaDb, MS SQL, Oracle, DB2, HSQL and H2
            .build());
  }
}
