package bio.terra.pearl.core.service.participant.search.facets;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class StringFacetValue implements FacetValue {
  private List<String> values;
  private String keyName;

  public StringFacetValue(String keyName, List<String> values) {
    this.values = values;
    this.keyName = keyName;
  }
}
