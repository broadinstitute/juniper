package bio.terra.pearl.core.service.participant.search.facets.sql;

import bio.terra.pearl.core.service.participant.search.facets.IntRangeFacetValue;
import java.util.List;
import org.jdbi.v3.core.statement.Query;

public class ProfileAgeFacetSqlGenerator implements FacetSqlGenerator<IntRangeFacetValue> {
  public ProfileAgeFacetSqlGenerator() {}

  @Override
  public String getTableName() {
    return "profile";
  }

  @Override
  public String getJoinQuery() {
    return " left join profile on enrollee.profile_id = profile.id";
  }

  @Override
  public String getSelectQuery(IntRangeFacetValue facetValue, int facetIndex) {
    return null; // already included in base query
  }

  @Override
  public String getWhereClause(IntRangeFacetValue facetValue, int facetIndex) {
    int maxValue = facetValue.getMax() != null ? facetValue.getMax() : 150;
    int minValue = facetValue.getMin() != null ? facetValue.getMin() : 0;
    // CAREFUL: this is putting user params directly in sql, this is only safe since it's only allowing integers.
    return """
         AGE(profile.birth_date) > (interval '%d years') and AGE(profile.birth_date) < (interval '%d years')
        """.formatted(minValue, maxValue);
  }

  @Override
  public String getCombinedWhereClause(List<IntRangeFacetValue> facetValues) {
    return "";
  }

  @Override
  public void bindSqlParameters(IntRangeFacetValue facetValue, int facetIndex, Query query) {
    // no-op, see above comment about not using bindings
  }
}


