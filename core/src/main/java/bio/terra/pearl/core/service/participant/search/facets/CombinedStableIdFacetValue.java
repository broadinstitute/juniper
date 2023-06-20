package bio.terra.pearl.core.service.participant.search.facets;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CombinedStableIdFacetValue implements FacetValue {
  private String keyName;
  private List<StableIdStringFacetValue> values;
  public CombinedStableIdFacetValue(String keyName, List<StableIdStringFacetValue> values) {
    this.values = values;
    this.keyName = keyName;
  }
}
