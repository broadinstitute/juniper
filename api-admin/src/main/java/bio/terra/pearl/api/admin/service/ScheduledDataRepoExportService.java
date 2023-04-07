package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.core.service.datarepo.DataRepoExportService;
import java.util.concurrent.TimeUnit;
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

  /* Initialize any missing datasets every hour or so. We'll have one dataset per study
    environment. By decoupling dataset initialization from ingest, we don't have to worry
    about handling large async saga transactions. If a dataset has not yet been initialized, ingest
    will simply be skipped until the dataset is ready. These operations happen frequently enough
    that it shouldn't be a problem if the first round of ingest is delayed due to an uninitialized
    dataset: by the next round, it should be ready.
  */
    @Scheduled(timeUnit = TimeUnit.HOURS, fixedDelay = 1, initialDelay = 0)
  //  @SchedulerLock(
  //      name = "DataRepoExportService.initializeStudyEnvironmentDatasets",
  //      lockAtMostFor = "30m",
  //      lockAtLeastFor = "5m")
  public void initializeStudyEnvironmentDatasets() {
    logger.info("Initializing data sets...");
    dataRepoExportService.initializeStudyEnvironmentDatasets();
  }

  @Scheduled(timeUnit = TimeUnit.MINUTES, fixedDelay = 1, initialDelay = 0)
  // TODO: ShedLock
  public void pollRunningInitializeJobs() {
    logger.info("Polling running TDR dataset initialize jobs...");
    dataRepoExportService.pollRunningInitializeJobs();
  }

  @Scheduled(timeUnit = TimeUnit.MINUTES, fixedDelay = 5, initialDelay = 0)
  // TODO: ShedLock
  public void pollRunningIngestJobs() {
    logger.info("Polling running TDR dataset ingest jobs...");
    // TODO
  }
}
