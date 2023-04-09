package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.core.service.datarepo.DataRepoExportService;
import java.util.concurrent.TimeUnit;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledDataRepoExportService {
  private static final Logger logger = LoggerFactory.getLogger(DataRepoExportService.class);
  private DataRepoExportService dataRepoExportService;

  public ScheduledDataRepoExportService(DataRepoExportService dataRepoExportService) {
    this.dataRepoExportService = dataRepoExportService;
  }

  @Scheduled(
      timeUnit = TimeUnit.MINUTES,
      fixedDelay = 10,
      initialDelay = 0) // Execute every 10 minutes
  public void pingDataRepoStatus() {
    logger.info("Pinging Terra Data Repo. Up: " + dataRepoExportService.getServiceStatus());
  }

  /* Create any missing datasets every hour or so. There is one dataset per study
    environment. By decoupling dataset creation from ingest, we don't have to worry
    about handling large async saga transactions. If a dataset has not yet been created, ingest
    will simply be skipped until the dataset is ready. These operations happen frequently enough
    that it shouldn't be a problem if the first round of ingest is delayed due to a missing
    dataset: by the next round, it should be ready.
  */
  @Scheduled(timeUnit = TimeUnit.MINUTES, fixedDelay = 60, initialDelay = 5)
  @SchedulerLock(
      name = "DataRepoExportService.createDatasetsForStudyEnvironments",
      lockAtMostFor = "10m",
      lockAtLeastFor = "5m")
  public void initializeStudyEnvironmentDatasets() {
    logger.info("Creating datasets...");
    dataRepoExportService.createDatasetsForStudyEnvironments();
  }

  @Scheduled(timeUnit = TimeUnit.MINUTES, fixedDelay = 10, initialDelay = 10)
  @SchedulerLock(
      name = "DataRepoExportService.pollRunningCreateDatasetJobs",
      lockAtMostFor = "5m",
      lockAtLeastFor = "2m")
  public void pollRunningInitializeJobs() {
    logger.info("Polling running TDR dataset creation jobs...");
    dataRepoExportService.pollRunningCreateDatasetJobs();
  }
}
