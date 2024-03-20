package bio.terra.pearl.core.service.search.expressions;

import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpression;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import org.jooq.Operator;

import java.util.UUID;

public class BooleanSearchExpression implements EnrolleeSearchExpression {

    private final EnrolleeSearchExpression left;
    private final EnrolleeSearchExpression right;
    private final Operator operator;

    public BooleanSearchExpression(EnrolleeSearchExpression left, EnrolleeSearchExpression right, Operator operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    @Override
    public boolean evaluate(EnrolleeSearchContext enrolleeCtx) {
        return switch (operator) {
            case AND -> left.evaluate(enrolleeCtx) && right.evaluate(enrolleeCtx);
            case OR -> left.evaluate(enrolleeCtx) || right.evaluate(enrolleeCtx);
            case XOR -> left.evaluate(enrolleeCtx) ^ right.evaluate(enrolleeCtx);
        };
    }

    @Override
    public EnrolleeSearchQueryBuilder generateQueryBuilder(UUID studyEnvId) {
        EnrolleeSearchQueryBuilder leftSQL = left.generateQueryBuilder(studyEnvId);
        EnrolleeSearchQueryBuilder rightSQL = right.generateQueryBuilder(studyEnvId);
        return leftSQL.merge(rightSQL, operator);
    }

}
