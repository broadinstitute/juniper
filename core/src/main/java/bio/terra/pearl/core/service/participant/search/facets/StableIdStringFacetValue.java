package bio.terra.pearl.core.service.participant.search.facets;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class StableIdStringFacetValue extends StringFacetValue {
  private String stableId;
  public StableIdStringFacetValue(String keyName, String stableId, List<String> values) {
    super(keyName, values);
    this.stableId = stableId;
  }
}
