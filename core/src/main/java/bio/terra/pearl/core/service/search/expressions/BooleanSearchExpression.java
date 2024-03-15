package bio.terra.pearl.core.service.search.expressions;

import bio.terra.pearl.core.service.search.BooleanOperator;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.SQLSearch;

import java.util.UUID;

public class BooleanSearchExpression implements EnrolleeSearchExpression {

    private final EnrolleeSearchExpression left;
    private final EnrolleeSearchExpression right;
    private final BooleanOperator operator;

    public BooleanSearchExpression(EnrolleeSearchExpression left, EnrolleeSearchExpression right, BooleanOperator operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    @Override
    public boolean evaluate(EnrolleeSearchContext enrolleeCtx) {
        return switch (operator) {
            case AND -> left.evaluate(enrolleeCtx) && right.evaluate(enrolleeCtx);
            case OR -> left.evaluate(enrolleeCtx) || right.evaluate(enrolleeCtx);
        };
    }

    @Override
    public SQLSearch generateSqlSearch(UUID studyEnvId) {
        SQLSearch leftSQL = left.generateSqlSearch(studyEnvId);
        SQLSearch rightSQL = right.generateSqlSearch(studyEnvId);
        return leftSQL.merge(rightSQL, operator);
    }

}
