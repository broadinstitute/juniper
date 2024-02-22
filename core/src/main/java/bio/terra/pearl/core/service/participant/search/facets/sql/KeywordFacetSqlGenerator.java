package bio.terra.pearl.core.service.participant.search.facets.sql;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.service.participant.search.EnrolleeSearchUtils;
import bio.terra.pearl.core.service.participant.search.facets.StringFacetValue;
import org.jdbi.v3.core.statement.Query;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class KeywordFacetSqlGenerator implements FacetSqlGenerator<StringFacetValue> {

    public KeywordFacetSqlGenerator() {}

    @Override
    public String getTableName() {
        // this facet is only tied to enrollee and Profile tables that are already joined
        return "";
    }

    @Override
    public String getJoinQuery() {
        return "";
    }

    @Override
    public String getSelectQuery(StringFacetValue facetValue, int facetIndex) {
        return null; // already included in base query
    }

    @Override
    public String getWhereClause(StringFacetValue facetValue, int facetIndex) {
        if (facetValue.getValues().isEmpty()) {
            return " 1 = 1";
        }
        return IntStream.range(0, facetValue.getValues().size())
                .mapToObj(index -> {
                    String paramName = EnrolleeSearchUtils.getSqlParamName("keyword", facetValue.getKeyName(), index);
                    return """
                            (profile.given_name ilike :%1$s 
                              OR profile.family_name ilike :%1$s
                              OR profile.contact_email ilike :%1$s
                              OR enrollee.shortcode ilike :%1$s)
                            """
                            .formatted(paramName);
                })
                .collect(Collectors.joining(" AND"));
    }

    @Override
    public String getCombinedWhereClause(List<StringFacetValue> facetValues) {
        return "";
    }

    @Override
    public void bindSqlParameters(StringFacetValue facetValue, int facetIndex, Query query) {
        for(int i = 0; i < facetValue.getValues().size(); i++) {
            query.bind(EnrolleeSearchUtils.getSqlParamName("keyword",
                    facetValue.getKeyName(), i), "%" + facetValue.getValues().get(i) + "%");
        }
    }
}
