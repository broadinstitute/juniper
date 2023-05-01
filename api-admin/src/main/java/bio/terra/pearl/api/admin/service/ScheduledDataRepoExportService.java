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

  /* Create any missing datasets every hour or so. There is one dataset per study
    environment. By decoupling dataset creation from ingest, we don't have to worry
    about handling large async saga transactions. If a dataset has not yet been created, ingest
    will simply be skipped until the dataset is ready. These operations happen frequently enough
    that it shouldn't be a problem if the first round of ingest is delayed due to a missing
    dataset: by the next round, it should be ready.
  */
  @Scheduled(timeUnit = TimeUnit.MINUTES, fixedDelay = 60, initialDelay = 0)
  @SchedulerLock(
      name = "DataRepoExportService.createDatasetsForStudyEnvironments",
      lockAtMostFor = "10m",
      lockAtLeastFor = "5m")
  public void createStudyEnvironmentDatasets() {
    if (isTdrConfigured()) {
      logger.info("Creating datasets...");
      dataRepoExportService.createDatasetsForStudyEnvironments();
    } else {
      logger.error(
          "Error: Skipping TDR dataset creation, as TDR has not been configured for this environment.");
    }
  }

  @Scheduled(timeUnit = TimeUnit.MINUTES, fixedDelay = 240, initialDelay = 0)
  @SchedulerLock(
      name = "DataRepoExportService.ingestStudyEnvironmentDatasets",
      lockAtMostFor = "10m",
      lockAtLeastFor = "5m")
  public void ingestStudyEnvironmentDatasets() {
    if (isTdrConfigured()) {
      logger.info("Ingesting datasets...");
      dataRepoExportService.ingestDatasets();
    } else {
      logger.error(
          "Error: Skipping TDR dataset ingest, as TDR has not been configured for this environment.");
    }
  }

  @Scheduled(timeUnit = TimeUnit.MINUTES, fixedDelay = 1, initialDelay = 0)
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
            "deploymentZone",
            "storageAccountName",
            "storageAccountKey",
            "storageContainerName");

    return REQUIRED_TDR_ENV_VARS.stream()
        .allMatch(
            envVar -> StringUtils.isNotBlank(env.getProperty(String.format("env.tdr.%s", envVar))));
  }
}
