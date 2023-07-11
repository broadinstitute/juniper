package bio.terra.pearl.populate.dto;

import java.time.Duration;
import java.time.Instant;

public interface TimeShiftable {
  Integer getSubmittedHoursAgo();
  default boolean isTimeShifted() {
    return getSubmittedHoursAgo() != null;
  }

  default Instant shiftedInstant() {
    return Instant.now().minus(Duration.ofHours(getSubmittedHoursAgo()));
  }
}
