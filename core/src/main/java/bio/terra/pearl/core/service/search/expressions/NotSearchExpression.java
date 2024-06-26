package bio.terra.pearl.core.service.search.expressions;

import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpression;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;

import java.util.UUID;

/**
 * High level search expression which inverts the expression inside of it.
 */
public class NotSearchExpression implements EnrolleeSearchExpression {
    private final EnrolleeSearchExpression inner;

    public NotSearchExpression(EnrolleeSearchExpression inner) {
        this.inner = inner;
    }

    @Override
    public boolean evaluate(EnrolleeSearchContext enrolleeCtx) {
        return !inner.evaluate(enrolleeCtx);
    }

    @Override
    public EnrolleeSearchQueryBuilder generateQueryBuilder(UUID studyEnvId) {
        EnrolleeSearchQueryBuilder innerSQL = inner.generateQueryBuilder(studyEnvId);
        innerSQL.setWhereConditions(innerSQL.getWhereConditions().not());
        return innerSQL;
    }

}
