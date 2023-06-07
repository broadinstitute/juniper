package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.core.service.kit.KitRequestService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledKitStatusService {
  private static final Logger logger = LoggerFactory.getLogger(ScheduledKitStatusService.class);
  private final KitRequestService kitRequestService;

  public ScheduledKitStatusService(KitRequestService kitRequestService) {
    this.kitRequestService = kitRequestService;
  }
  /**
   * Update kit statuses from Pepper at 12:30am every day. We're _very_ generous with lockAtMostFor
   * because this only runs once per day.
   */
  @Scheduled(cron = "0 30 0 * * *")
  @SchedulerLock(
      name = "KitRequestService.updateAllKitStatuses",
      lockAtLeastFor = "1m",
      lockAtMostFor = "360m")
  public void fetchUpdatedKitStatuses() {
    logger.info("Updating kit status from Pepper...");
    kitRequestService.syncAllKitStatusesFromPepper();
    logger.info("Finished updating kit status.");
  }
}
