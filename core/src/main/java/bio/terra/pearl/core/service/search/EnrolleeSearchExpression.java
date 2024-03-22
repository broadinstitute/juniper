package bio.terra.pearl.core.service.search;

import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import org.jooq.Query;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.UUID;

/**
 * An expression that can be used to filter or search for enrollees. These should be created only by the
 * {@link EnrolleeSearchExpressionParser}.
 * <br>
 * To filter enrollees, you can use the `evaluate` method. To run the expression as a SQL query, you must
 * pass it to the {@link bio.terra.pearl.core.dao.search.EnrolleeSearchExpressionDao}.
 */
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
