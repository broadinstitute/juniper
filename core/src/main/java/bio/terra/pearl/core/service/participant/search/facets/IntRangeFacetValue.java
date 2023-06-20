package bio.terra.pearl.core.service.participant.search.facets;

import bio.terra.pearl.core.service.participant.search.facets.FacetValue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class IntRangeFacetValue implements FacetValue {
  private String keyName;
  private Integer min;
  private Integer max;

  public IntRangeFacetValue(String keyName, Integer min, Integer max) {
    this.keyName = keyName;
    this.min = min;
    this.max = max;
  }
}
