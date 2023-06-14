package bio.terra.pearl.core.service.participant.search;

import java.util.List;
import lombok.Getter;

@Getter
public class StableIdFacetValue extends StringFacetValue {
  private String stableId;
  public StableIdFacetValue(String keyName, List<String> filterValues, String stableId) {
    super(keyName, filterValues);
    this.stableId = stableId;
  }
}
