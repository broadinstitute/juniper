package bio.terra.pearl.core.service.participant.search.facets;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jdbi.v3.core.statement.Query;

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

  @Override
  public String getWhereClause(String tableName, String columnName, int facetIndex) {
    int maxValue = getMax() != null ? getMax() : Integer.MAX_VALUE;
    int minValue = getMin() != null ? getMin() : Integer.MIN_VALUE;
    // CAREFUL: this is putting user params directly in sql, this is only safe since it's only allowing integers.
    return """
         %s.%s >= %d and %s.%s < %d
        """.formatted(tableName, columnName, minValue, tableName, columnName, maxValue);
  }

  @Override
  public void bindSqlParameters(String tableName, String columnName, int facetIndex, Query query) {
    // no-op, see above comment about not using bindings
  }
}
