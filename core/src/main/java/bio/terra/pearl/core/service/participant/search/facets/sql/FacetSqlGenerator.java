package bio.terra.pearl.core.service.participant.search.facets.sql;

import bio.terra.pearl.core.service.participant.search.facets.FacetValue;
import java.util.List;
import org.jdbi.v3.core.statement.Query;

public interface FacetSqlGenerator<T extends FacetValue> {
  String getTableName();
  String getSelectQuery(T facetValue);
  String getJoinQuery();
  String getWhereClause(T facetValue, int facetIndex);
  String getCombinedWhereClause(List<T> facetValues);

  void bindSqlParameters(T facetValue, int facetIndex, Query query);
}
