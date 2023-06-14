package bio.terra.pearl.core.service.participant.search;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import java.util.List;
import org.jdbi.v3.core.statement.Query;

public class ParticipantTaskFacetValue implements SqlSearchable<ParticipantTaskFacetValue> {
  private static final String TABLE_NAME = "participant_task";
  private final String columnName;
  private final StableIdFacetValue facetValue;

  public ParticipantTaskFacetValue(StableIdFacetValue facetValue) {
    this.facetValue = facetValue;
    this.columnName = BaseJdbiDao.toSnakeCase(facetValue.getKeyName());
  }

  @Override
  public String getTableName() {
    return TABLE_NAME;
  }

  @Override
  public String getJoinQuery() {
    return " left join %s on %s.enrollee_id = enrollee.id".formatted(TABLE_NAME, TABLE_NAME);
  }

  @Override
  public String getSelectQuery() {
    return " array_agg(%s.%s) AS %s__%s, array_agg(%s.target_stable_id) AS %s__target_stable_id"
        .formatted(TABLE_NAME, columnName, TABLE_NAME, columnName, TABLE_NAME, TABLE_NAME);
  }

  @Override
  public String getWhereClause(int facetIndex) {
    return """
         exists (select 1 from %s where %s.enrollee_id = enrollee.id
         and %s.target_stable_id = :%s
         and %s.%s = :%s)
        """.formatted(TABLE_NAME, TABLE_NAME, TABLE_NAME, getStableIdParam(facetIndex), TABLE_NAME, columnName,
        EnrolleeSearchUtils.getSqlParamName(TABLE_NAME, columnName, facetIndex));
  }

  @Override
  public String getCombinedWhereClause(List<ParticipantTaskFacetValue> facetValues) {
    return "";
  }

  @Override
  public void bindSqlParameters(int facetIndex, Query query) {
    EnrolleeSearchUtils.bindSqlParameters(facetValue, TABLE_NAME, query);
    query.bind(getStableIdParam(facetIndex), facetValue.getStableId());
  }

  private String getStableIdParam(int facetIndex) {
    return "participantTaskStableId%".formatted(facetIndex);
  }

}
