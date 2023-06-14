package bio.terra.pearl.core.service.participant.search;

import java.util.List;
import org.jdbi.v3.core.statement.Query;

public interface SqlSearchable<T> {
  String getTableName();
  String getSelectQuery();
  String getJoinQuery();
  String getWhereClause(int facetIndex);
  String getCombinedWhereClause(List<T> facetValues);

  void bindSqlParameters(int facetIndex, Query query);
}
