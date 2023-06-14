package bio.terra.pearl.core.service.participant.search;

import java.util.List;
import lombok.Getter;

@Getter
public class StringFacetValue implements FacetValue {
  private final List<String> filterValues;
  private final String keyName;

  public StringFacetValue(String keyName, List<String> filterValues) {
    this.filterValues = filterValues;
    this.keyName = keyName;
  }
}
