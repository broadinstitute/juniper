package bio.terra.pearl.core.service.participant.search.facets.sql;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.service.participant.search.EnrolleeSearchUtils;
import bio.terra.pearl.core.service.participant.search.facets.StringFacetValue;
import java.util.List;
import org.jdbi.v3.core.statement.Query;

public class ProfileFacetSqlGenerator implements FacetSqlGenerator<StringFacetValue> {

    public ProfileFacetSqlGenerator() {}

    private String getColumnName(StringFacetValue facetValue) {
      return BaseJdbiDao.toSnakeCase(facetValue.getKeyName());
    }

    @Override
    public String getTableName() {
      return "profile";
    }

    @Override
    public String getJoinQuery() {
      return " left join profile on enrollee.profile_id = profile.id";
    }

    @Override
    public String getSelectQuery(StringFacetValue facetValue) {
      return null; // already included in base query
    }

    @Override
    public String getWhereClause(StringFacetValue facetValue, int facetIndex) {
      String columnName = getColumnName(facetValue);
      return EnrolleeSearchUtils.getSimpleWhereClause(facetValue, columnName, "profile");
    }

  @Override
  public String getCombinedWhereClause(List<StringFacetValue> facetValues) {
    return "";
  }

  @Override
  public void bindSqlParameters(StringFacetValue facetValue, int facetIndex, Query query) {
      EnrolleeSearchUtils.bindSqlParameters(facetValue, "profile", query);
  }
}
