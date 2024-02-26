package bio.terra.pearl.core.service.participant.search.facets.sql;

import bio.terra.pearl.core.service.participant.search.facets.AnswerFacetValue;

public class AnswerFacetSqlGenerator extends BaseFacetSqlGenerator<AnswerFacetValue> {
    private static final String TABLE_NAME = "answer";

    public AnswerFacetSqlGenerator() {}

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getJoinQuery() {
        return " left join answer on answer.enrollee_id = enrollee.id";
    }

    @Override
    public String getColumnName(AnswerFacetValue facetValue) {
        if (facetValue.getBooleanValue() != null) {
            return "boolean_value";
        } else if (facetValue.getNumberValue() != null) {
            return "number_value";
        }
        return "string_value";
    }

    @Override
    public String getSelectQuery(AnswerFacetValue facetValue, int facetIndex) {
        return " answer.%s AS answer_%s".formatted(getColumnName(facetValue), facetIndex);
    }
}
