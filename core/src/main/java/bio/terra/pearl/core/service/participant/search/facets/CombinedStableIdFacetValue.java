package bio.terra.pearl.core.service.participant.search.facets;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jdbi.v3.core.statement.Query;

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

  @Override
  public String getWhereClause(String tableName, String columnName, int facetIndex) {
    return IntStream.range(0, getValues().size()).mapToObj(i -> {
      StableIdStringFacetValue subValue = getValues().get(i);
      String subFilterOrClause = IntStream.range(0, subValue.getValues().size()).mapToObj(j ->
              "%s.%s = :%s".formatted(tableName, columnName, getSqlParamName(tableName, columnName, facetIndex, i, j))
      ).collect(Collectors.joining(" OR "));
      if (subValue.getValues().size() == 0) {
        subFilterOrClause = "1 = 1";
      }
      return """
         exists (select 1 from %s where %s.enrollee_id = enrollee.id
         and %s.target_stable_id = :%s
         and (%s))
        """.formatted(tableName, tableName, tableName, getStableIdParam(facetIndex, i), subFilterOrClause);
    }).collect(Collectors.joining(" and"));
  }

  @Override
  public void bindSqlParameters(String tableName, String columnName, int facetIndex, Query query) {
    for(int i = 0; i < getValues().size(); i++) {
      StableIdStringFacetValue subVal = getValues().get(i);
      query.bind(getStableIdParam(facetIndex, i), subVal.getStableId());
      for (int j = 0; j < subVal.getValues().size(); j++) {
        query.bind(getSqlParamName(tableName, columnName, facetIndex, i, j), subVal.getValues().get(j));
      }
    }
  }


  private String getSqlParamName(String tableName, String columnName, int facetIndex, int subIndex, int subFilterIndex) {
    return "%s_%d_%d".formatted(getSqlParamName(tableName, columnName, facetIndex), subIndex, subFilterIndex);
  }

  private String getStableIdParam(int facetIndex, int subIndex) {
    return "participantTaskStableId%d_%d".formatted(facetIndex, subIndex);
  }

}
