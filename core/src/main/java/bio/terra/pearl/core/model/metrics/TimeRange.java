package bio.terra.pearl.core.model.metrics;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter @Setter @AllArgsConstructor
public class TimeRange {
  private Instant start;
  private Instant end;
}
