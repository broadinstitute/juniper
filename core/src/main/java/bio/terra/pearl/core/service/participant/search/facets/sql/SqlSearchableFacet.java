package bio.terra.pearl.core.service.participant.search.facets.sql;

import bio.terra.pearl.core.service.participant.search.facets.FacetValue;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jdbi.v3.core.statement.Query;

@AllArgsConstructor @Getter
public class SqlSearchableFacet<T extends FacetValue> {
  T value;
  FacetSqlGenerator<T> sqlGenerator;

  public String getTableName() {
    return sqlGenerator.getTableName();
  };
  public String getSelectQuery() {
    return sqlGenerator.getSelectQuery(value);
  };
  public String getJoinQuery() {
    return sqlGenerator.getJoinQuery();
  };
  public String getWhereClause(int facetIndex) {
    return sqlGenerator.getWhereClause(value, facetIndex);
  }
  public String getCombinedWhereClause(List<T> facetValues) {
    return sqlGenerator.getCombinedWhereClause(facetValues);
  };

  public void bindSqlParameters(int facetIndex, Query query) {
    sqlGenerator.bindSqlParameters(value, facetIndex, query);
  }

}
