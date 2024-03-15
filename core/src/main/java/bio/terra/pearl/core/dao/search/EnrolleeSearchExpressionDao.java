package bio.terra.pearl.core.dao.search;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.service.search.expressions.EnrolleeSearchExpression;
import bio.terra.pearl.core.service.search.sql.SQLSearch;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class EnrolleeSearchExpressionDao {
    private final Jdbi jdbi;
    public EnrolleeSearchExpressionDao(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public List<Enrollee> executeSearch(EnrolleeSearchExpression expression, UUID studyEnvId) {
        return executeSearch(expression.generateSqlSearch(studyEnvId));
    }

    public List<Enrollee> executeSearch(SQLSearch search) {
        return jdbi.withHandle(handle -> {
            Query query = handle.createQuery(search.generateQueryString());
            search.bindSqlParams(query);
            return query.mapTo(Enrollee.class).list();
        });
    }
}
