package bio.terra.pearl.core.service.participant.search.facets.sql;

import bio.terra.pearl.core.service.participant.search.facets.CombinedStableIdFacetValue;


public class ParticipantTaskFacetSqlGenerator extends BaseFacetSqlGenerator<CombinedStableIdFacetValue> {
  private static final String TABLE_NAME = "participant_task";

  public ParticipantTaskFacetSqlGenerator() {}

  @Override
  public String getTableName() {
    return TABLE_NAME;
  }

  @Override
  public String getJoinQuery() {
    return " left join %s on %s.enrollee_id = enrollee.id".formatted(TABLE_NAME, TABLE_NAME);
  }

  @Override
  public String getSelectQuery(CombinedStableIdFacetValue facetValue, int facetIndex) {
    String columnName = getColumnName(facetValue);
    // this will only return a single matched task, we'll need to extend this if we want to return more complex stuff
    return " %s.%s AS %s__%s, %s.target_stable_id AS %s__target_stable_id"
        .formatted(TABLE_NAME, columnName, TABLE_NAME, columnName, TABLE_NAME, TABLE_NAME);
  }
}
