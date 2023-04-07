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

  @Scheduled(timeUnit = TimeUnit.HOURS, fixedDelay = 4, initialDelay = 0) // Execute every 4 hours
  public void pingDataRepoStatus() {
    logger.info("Pinging Terra Data Repo. Up: " + dataRepoExportService.getServiceStatus());
  }
}
