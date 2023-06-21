package bio.terra.pearl.core.service.participant.search.facets.sql;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.service.participant.search.EnrolleeSearchUtils;
import bio.terra.pearl.core.service.participant.search.facets.CombinedStableIdFacetValue;
import bio.terra.pearl.core.service.participant.search.facets.StableIdStringFacetValue;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jdbi.v3.core.statement.Query;

public class ParticipantTaskFacetSqlGenerator implements FacetSqlGenerator<CombinedStableIdFacetValue> {
  private static final String TABLE_NAME = "participant_task";

  public ParticipantTaskFacetSqlGenerator() {}

  private String getColumnName(CombinedStableIdFacetValue facetValue) {
    return BaseJdbiDao.toSnakeCase(facetValue.getKeyName());
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
  public String getSelectQuery(CombinedStableIdFacetValue facetValue) {
    String columnName = getColumnName(facetValue);
    return " array_agg(%s.%s) AS %s__%s, array_agg(%s.target_stable_id) AS %s__target_stable_id"
        .formatted(TABLE_NAME, columnName, TABLE_NAME, columnName, TABLE_NAME, TABLE_NAME);
  }

  @Override
  public String getWhereClause(CombinedStableIdFacetValue facetValue, int facetIndex) {
    String columnName = getColumnName(facetValue);
    return IntStream.range(0, facetValue.getValues().size()).mapToObj(i -> {
      StableIdStringFacetValue subValue = facetValue.getValues().get(i);
      String subFilterOrClause = IntStream.range(0, subValue.getValues().size()).mapToObj(j ->
        "%s.%s = :%s".formatted(TABLE_NAME, columnName, getSqlParamName(TABLE_NAME, columnName, facetIndex, i, j))
      ).collect(Collectors.joining(" OR "));
      if (subValue.getValues().size() == 0) {
        subFilterOrClause = "1 = 1";
      }
      return """
         exists (select 1 from %s where %s.enrollee_id = enrollee.id
         and %s.target_stable_id = :%s
         and (%s))
        """.formatted(TABLE_NAME, TABLE_NAME, TABLE_NAME, getStableIdParam(facetIndex, i), subFilterOrClause);
    }).collect(Collectors.joining(" and"));
  }

  @Override
  public String getCombinedWhereClause(List<CombinedStableIdFacetValue> facetValues) {
    return "";
  }

  @Override
  public void bindSqlParameters(CombinedStableIdFacetValue facetValue, int facetIndex, Query query) {
    String columnName = getColumnName(facetValue);
    for(int i = 0; i < facetValue.getValues().size(); i++) {
      StableIdStringFacetValue subVal = facetValue.getValues().get(i);
      query.bind(getStableIdParam(facetIndex, i), subVal.getStableId());
      for (int j = 0; j < subVal.getValues().size(); j++) {
        query.bind(getSqlParamName(TABLE_NAME, columnName, facetIndex, i, j), subVal.getValues().get(j));
      }
    }
  }

  private String getStableIdParam(int facetIndex, int subIndex) {
    return "participantTaskStableId%d_%d".formatted(facetIndex, subIndex);
  }

  public static String getSqlParamName(String tableName, String columnName, int facetIndex, int subIndex, int subFilterIndex) {
    return "%s_%d_%d".formatted(EnrolleeSearchUtils.getSqlParamName(tableName, columnName, facetIndex), subIndex, subFilterIndex);
  }

}
