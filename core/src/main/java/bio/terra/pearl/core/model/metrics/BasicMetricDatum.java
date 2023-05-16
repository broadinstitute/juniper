package bio.terra.pearl.core.model.metrics;

import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class BasicMetricDatum {
  private String name;
  private String subcategory;
  private Instant time;
}
