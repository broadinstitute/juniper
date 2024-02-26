package bio.terra.pearl.core.service.participant.search.facets.sql;

import bio.terra.pearl.core.service.participant.search.facets.FacetValue;

public class ProfileFacetSqlGenerator extends BaseFacetSqlGenerator<FacetValue> {
    public ProfileFacetSqlGenerator() {}

    @Override
    protected String getColumnName(FacetValue facetValue) {
        if ("age".equals(facetValue.getKeyName())) {
            return "birth_date";
        }
        return super.getColumnName(facetValue);
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
    public String getSelectQuery(FacetValue facetValue, int facetIndex) {
      return null; // already included in base query
    }
}
