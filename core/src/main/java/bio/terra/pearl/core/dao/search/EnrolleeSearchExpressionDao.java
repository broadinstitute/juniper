package bio.terra.pearl.core.dao.search;

import bio.terra.pearl.core.model.search.EnrolleeSearchResult;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpression;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class EnrolleeSearchExpressionDao {
    private final Jdbi jdbi;
    public EnrolleeSearchExpressionDao(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public List<EnrolleeSearchResult> executeSearch(EnrolleeSearchExpression expression, UUID studyEnvId) {
        return executeSearch(expression.generateQueryBuilder(studyEnvId));
    }

    private List<EnrolleeSearchResult> executeSearch(EnrolleeSearchQueryBuilder search) {
        return jdbi.withHandle(handle -> {
            org.jooq.Query jooqQuery = search.toQuery(DSL.using(SQLDialect.POSTGRES));
            Query query = handle.createQuery(jooqQuery.getSQL());
            for (int i = 0; i < jooqQuery.getBindValues().size(); i++) {
                query.bind(i, jooqQuery.getBindValues().get(i));
            }
            return query
                    .registerRowMapper(EnrolleeSearchResult.class, new EnrolleeSearchResultMapper())
                    .reduceRows(new EnrolleeSearchResultReducer())
                    .toList();
        });
    }
}
