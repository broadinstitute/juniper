package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.api.admin.config.StatusCheckConfiguration;
import bio.terra.pearl.api.admin.model.SystemStatus;
import bio.terra.pearl.api.admin.model.SystemStatusSystems;
import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BaseStatusService {
  /** cached status */
  private final AtomicReference<SystemStatus> cachedStatus;

  /** configuration parameters */
  private final StatusCheckConfiguration configuration;

  /** set of status methods to check */
  private final ConcurrentHashMap<String, Supplier<SystemStatusSystems>> statusCheckMap;

  /** scheduler */
  private final ScheduledExecutorService scheduler;

  /** last time cache was updated */
  private final AtomicReference<Instant> lastStatusUpdate;

  public BaseStatusService(StatusCheckConfiguration configuration) {
    this.configuration = configuration;
    statusCheckMap = new ConcurrentHashMap<>();
    cachedStatus = new AtomicReference<>(new SystemStatus().ok(false));
    lastStatusUpdate = new AtomicReference<>(Instant.now());
    scheduler = Executors.newScheduledThreadPool(1);
  }

  @PostConstruct
  private void startStatusChecking() {
    if (configuration.enabled()) {
      scheduler.scheduleAtFixedRate(
          this::checkStatus,
          configuration.startupWaitSeconds(),
          configuration.pollingIntervalSeconds(),
          TimeUnit.SECONDS);
    }
  }

  void registerStatusCheck(String name, Supplier<SystemStatusSystems> checkFn) {
    statusCheckMap.put(name, checkFn);
  }

  @VisibleForTesting
  void checkStatus() {
    if (configuration.enabled()) {
      SystemStatus newStatus = new SystemStatus();
      try {
        Map<String, SystemStatusSystems> systems =
            statusCheckMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
        newStatus.setOk(systems.values().stream().allMatch(SystemStatusSystems::isOk));
        newStatus.setSystems(systems);
      } catch (Exception e) {
        log.warn("Status check exception", e);
        newStatus.setOk(false);
      }
      cachedStatus.set(newStatus);
      lastStatusUpdate.set(Instant.now());
    }
  }

  public SystemStatus getCurrentStatus() {
    if (configuration.enabled()) {
      // If staleness time (last update + stale threshold) is before the current time, then
      // we are officially not OK.
      if (lastStatusUpdate
          .get()
          .plusSeconds(configuration.stalenessThresholdSeconds())
          .isBefore(Instant.now())) {
        log.warn("Status has not been updated since {}", lastStatusUpdate);
        cachedStatus.set(new SystemStatus().ok(false));
      }
      return cachedStatus.get();
    }
    return new SystemStatus().ok(true);
  }
}
