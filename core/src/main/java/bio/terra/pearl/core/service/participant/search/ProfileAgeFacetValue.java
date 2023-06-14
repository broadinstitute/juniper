package bio.terra.pearl.core.service.participant.search;

import java.util.List;
import org.jdbi.v3.core.statement.Query;

public class ProfileAgeFacetValue implements SqlSearchable<ProfileAgeFacetValue> {
  private final IntRangeFacetValue facetValue;

  public ProfileAgeFacetValue(IntRangeFacetValue facetValue) {
    this.facetValue = facetValue;
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
    return " max(profile.birth_date) AS profile__birth_date";
  }

  @Override
  public String getWhereClause(int facetIndex) {
    int maxValue = facetValue.getMax() != null ? facetValue.getMax() : 150;
    int minValue = facetValue.getMin() != null ? facetValue.getMin() : 0;
    // CAREFUL: this is putting user params directly in sql, this is only safe since it's only allowing integers.
    return """
         AGE(profile.birth_date) > (interval '%d years') and AGE(profile.birth_date) < (interval '%d years')
        """.formatted(minValue, maxValue);
  }

  @Override
  public String getCombinedWhereClause(List<ProfileAgeFacetValue> facetValues) {
    return "";
  }

  @Override
  public void bindSqlParameters(int facetIndex, Query query) {
    // no-op, see above comment about not using bindings
  }
}
