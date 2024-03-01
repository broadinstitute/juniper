package bio.terra.pearl.core.service.participant.search.facets.sql;

import bio.terra.pearl.core.service.participant.search.facets.FacetValue;


public class EnrolleeFacetSqlGenerator extends BaseFacetSqlGenerator<FacetValue> {
    public EnrolleeFacetSqlGenerator() {}

    @Override
    public String getTableName() {
        return "enrollee";
    }

    @Override
    public String getJoinQuery() {
        return null; // already included in base query
    }

    @Override
    public String getSelectQuery(FacetValue facetValue, int facetIndex) {
        return null; // already included in base query
    }
}
