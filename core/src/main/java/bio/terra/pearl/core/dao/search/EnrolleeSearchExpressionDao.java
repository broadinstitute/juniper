package bio.terra.pearl.core.dao.search;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.search.EnrolleeSearchResult;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpression;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
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

    @RegisterBeanMapper(value = Enrollee.class, prefix = "enrollee")
    @RegisterBeanMapper(value = Profile.class, prefix = "profile")
    @RegisterRowMapper(EnrolleeSearchResultMapper.class)
    private List<EnrolleeSearchResult> executeSearch(EnrolleeSearchQueryBuilder search) {
        return jdbi.withHandle(handle -> {
            org.jooq.Query jooqQuery = search.toQuery(DSL.using(SQLDialect.POSTGRES));
            Query query = handle.createQuery(jooqQuery.getSQL());
            for (int i = 0; i < jooqQuery.getBindValues().size(); i++) {
                query.bind(i, jooqQuery.getBindValues().get(i));
            }
            return query
                    .registerRowMapper(EnrolleeSearchResult.class, new EnrolleeSearchResultMapper())
//                    .reduceRows(new EnrolleeSearchResultReducer())
                    .mapTo(EnrolleeSearchResult.class)
                    .list();
        });
    }
}
