package bio.terra.pearl.core.service.participant.search.facets;

import org.jdbi.v3.core.statement.Query;

/** A set of values that are parameters for a facet. */
public interface FacetValue {
  String getKeyName();
  void setKeyName(String keyName);
  default String getOperator() {
    return "=";
  };

  String getWhereClause(String tableName, String columnName, int facetIndex);

  void bindSqlParameters(String tableName, String columnName, int facetIndex, Query query);

  default String getSqlParamName(String tableName, String columnName, int facetIndex) {
    return "%s_%s_%d".formatted(tableName, columnName, facetIndex);
  }
}
