package bio.terra.pearl.core.service.participant.search.facets;

import lombok.Getter;
import lombok.Setter;
import org.jdbi.v3.core.statement.Query;

@Getter @Setter
public class BooleanFacetValue implements FacetValue {
  private String keyName;
  private Boolean value;

  public BooleanFacetValue(String keyName, Boolean value) {
    this.keyName = keyName;
    this.value = value;
  }


  @Override
  public String getWhereClause(String tableName, String columnName, int facetIndex) {
    if (getValue() == null) {
      return " 1 = 1";
    }
    return " %s.%s = :%s".formatted(tableName, columnName, getSqlParamName(tableName, columnName, facetIndex));

  }

  @Override
  public void bindSqlParameters(String tableName, String columnName, int facetIndex, Query query) {
    query.bind(getSqlParamName(tableName, columnName, facetIndex), getValue());
  }
}
