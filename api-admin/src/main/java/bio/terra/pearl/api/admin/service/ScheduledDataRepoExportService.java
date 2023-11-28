package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.core.service.datarepo.DataRepoExportService;
import com.google.common.collect.ImmutableSet;
import java.util.concurrent.TimeUnit;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledDataRepoExportService {
  private static final Logger logger = LoggerFactory.getLogger(DataRepoExportService.class);
  private DataRepoExportService dataRepoExportService;
  private Environment env;

  /* NOTE: Scheduled dataset creation and ingest was removed in https://github.com/broadinstitute/pearl/pull/367
    If you'd like to restore that functionality, reference that PR so you don't have to re-write the code.
  */

  public ScheduledDataRepoExportService(
      Environment env, DataRepoExportService dataRepoExportService) {
    this.env = env;
    this.dataRepoExportService = dataRepoExportService;
  }

  @Scheduled(timeUnit = TimeUnit.MINUTES, fixedDelay = 10, initialDelay = 0)
  public void pingDataRepoStatus() {
    if (isTdrConfigured()) {
      logger.info("Pinging Terra Data Repo. Up: " + dataRepoExportService.getServiceStatus());
    } else {
      logger.error(
          "Error: Skipping TDR status ping, as TDR has not been configured for this environment.");
    }
  }

  @Scheduled(timeUnit = TimeUnit.MINUTES, fixedDelay = 5, initialDelay = 0)
  @SchedulerLock(
      name = "DataRepoExportService.pollRunningJobs",
      lockAtMostFor = "5m",
      lockAtLeastFor = "1m")
  public void pollRunningJobs() {
    if (isTdrConfigured()) {
      logger.info("Polling running TDR jobs...");
      dataRepoExportService.pollRunningJobs();
    } else {
      logger.error(
          "Error: Skipping TDR job polling, as TDR has not been configured for this environment.");
    }
  }

  public boolean isTdrConfigured() {
    final ImmutableSet<String> REQUIRED_TDR_ENV_VARS =
        ImmutableSet.of(
            "serviceAccountCreds",
            "storageAccountName",
            "storageAccountKey",
            "storageContainerName");

    // Global on/off switch for TDR export
    boolean tdrExportEnabled = env.getProperty("env.tdr.tdrExportEnabled", Boolean.class, false);

    return tdrExportEnabled
        && REQUIRED_TDR_ENV_VARS.stream()
            .allMatch(
                envVar ->
                    StringUtils.isNotBlank(env.getProperty(String.format("env.tdr.%s", envVar))));
  }
}
