package bio.terra.pearl.core.service.search.expressions;

import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import org.jooq.Query;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.UUID;

public interface EnrolleeSearchExpression {
    default boolean evaluate(List<EnrolleeSearchContext> enrollees) {
        return enrollees.stream().allMatch(this::evaluate);
    }

    boolean evaluate(EnrolleeSearchContext enrollee);

    EnrolleeSearchQueryBuilder generateQueryBuilder(UUID studyEnvId);

    default Query generateQuery(UUID studyEnvId) {
        return this.generateQueryBuilder(studyEnvId).toQuery(DSL.using(SQLDialect.POSTGRES));
    }
}
