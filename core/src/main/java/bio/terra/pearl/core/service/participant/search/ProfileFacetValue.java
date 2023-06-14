package bio.terra.pearl.core.service.participant.search;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import java.util.List;
import org.jdbi.v3.core.statement.Query;

public class ProfileFacetValue implements SqlSearchable<ProfileFacetValue> {
    private final StringFacetValue facetValue;
    private final String columnName;

    public ProfileFacetValue(StringFacetValue facetValue) {
      this.facetValue = facetValue;
      this.columnName = BaseJdbiDao.toSnakeCase(facetValue.getKeyName());
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
    public String getSelectQuery() {
      return " max(profile.%s) AS profile__%s".formatted(columnName, columnName);
    }

    @Override
    public String getWhereClause(int facetIndex) {
      return EnrolleeSearchUtils.getSimpleWhereClause(facetValue, columnName, "profile");
    }

  @Override
  public String getCombinedWhereClause(List<ProfileFacetValue> facetValues) {
    return "";
  }

  @Override
  public void bindSqlParameters(int facetIndex, Query query) {
      EnrolleeSearchUtils.bindSqlParameters(facetValue, "profile", query);
  }
}
