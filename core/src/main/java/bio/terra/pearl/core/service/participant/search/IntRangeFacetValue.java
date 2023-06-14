package bio.terra.pearl.core.service.participant.search;

import lombok.Getter;

@Getter
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
