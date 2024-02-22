package bio.terra.pearl.core.service.participant.search.facets.sql;

import bio.terra.pearl.core.service.participant.search.facets.AnswerFacetValue;
import org.jdbi.v3.core.statement.Query;

import java.util.List;

public class AnswerFacetSqlGenerator implements FacetSqlGenerator<AnswerFacetValue> {
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

    @Override
    public String getWhereClause(AnswerFacetValue facetValue, int facetIndex) {
        return """
                 answer.%s = :%s
                 and answer.survey_stable_id = :%s
                 and answer.question_stable_id = :%s    
                """.formatted(
                        getColumnName(facetValue),
                        getValueParam(facetIndex),
                        getSurveyStableIdParam(facetIndex),
                        getQuestionStableIdParam(facetIndex)
                );
    }

    @Override
    public String getCombinedWhereClause(List<AnswerFacetValue> facetValues) {
        return "";
    }

    @Override
    public void bindSqlParameters(AnswerFacetValue facetValue, int facetIndex, Query query) {
        query.bind(getValueParam(facetIndex), facetValue.getValue());
        query.bind(getSurveyStableIdParam(facetIndex), facetValue.getSurveyStableId());
        query.bind(getQuestionStableIdParam(facetIndex), facetValue.getQuestionStableId());
    }

    private String getQuestionStableIdParam(int facetIndex) {
        return "question_stable_id_%s".formatted(facetIndex);
    }

    private String getSurveyStableIdParam(int facetIndex) {
        return "survey_stable_id_%s".formatted(facetIndex);
    }

    private String getValueParam(int facetIndex) {
        return "answer_val_%s".formatted(facetIndex);
    }
}
