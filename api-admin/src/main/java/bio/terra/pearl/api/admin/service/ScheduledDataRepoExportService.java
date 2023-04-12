package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.core.service.datarepo.DataRepoExportService;
import java.util.concurrent.TimeUnit;
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
      DataRepoExportService dataRepoExportService, Environment env) {
    this.dataRepoExportService = dataRepoExportService;
    this.env = env;
  }

  @Scheduled(timeUnit = TimeUnit.HOURS, fixedDelay = 4, initialDelay = 0) // Execute every 4 hours
  public void pingDataRepoStatus() {
    if (env.getProperty("env.tdr.serviceAccountCreds").equals("missing_tdr_sa_creds")) {
      logger.error("Skipping TDR ping: Missing TDR service account credentials");
      return;
    }
    logger.info("Pinging Terra Data Repo. Up: " + dataRepoExportService.getServiceStatus());
  }
}
