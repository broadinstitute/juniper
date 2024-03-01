package bio.terra.pearl.core.service.participant.search.facets;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jdbi.v3.core.statement.Query;

@Getter @Setter
@NoArgsConstructor
public class StringFacetValue implements FacetValue {
  private List<String> values;
  private String keyName;

  public StringFacetValue(String keyName, List<String> values) {
    this.values = values;
    this.keyName = keyName;
  }


  @Override
  public String getWhereClause(String tableName, String columnName, int facetIndex) {
    if (getValues().isEmpty()) {
      return " 1 = 1";
    }
    return IntStream.range(0, getValues().size())
            .mapToObj(index -> " %s.%s = :%s".formatted(tableName, columnName,
                    getSqlParamName(tableName, getKeyName() + facetIndex, index)))
            .collect(Collectors.joining(" OR"));
  }

  @Override
  public void bindSqlParameters(String tableName, String columnName, int facetIndex, Query query) {
    for(int i = 0; i < getValues().size(); i++) {
      query.bind(getSqlParamName(tableName, getKeyName() + facetIndex, i), getValues().get(i));
    }
  }
}
