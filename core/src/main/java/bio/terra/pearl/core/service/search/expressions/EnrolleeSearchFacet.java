package bio.terra.pearl.core.service.search.expressions;

import bio.terra.pearl.core.service.search.ComparisonOperator;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import bio.terra.pearl.core.service.search.terms.EnrolleeTermExtractor;
import bio.terra.pearl.core.service.search.terms.Term;
import org.jooq.Condition;
import org.jooq.Operator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.jooq.impl.DSL.condition;

public class EnrolleeSearchFacet implements EnrolleeSearchExpression {
    EnrolleeTermExtractor leftTermExtractor;
    EnrolleeTermExtractor rightTermExtractor;
    ComparisonOperator operator;

    public EnrolleeSearchFacet(EnrolleeTermExtractor leftTermExtractor, EnrolleeTermExtractor rightTermExtractor, ComparisonOperator operator) {
        this.leftTermExtractor = leftTermExtractor;
        this.rightTermExtractor = rightTermExtractor;
        this.operator = operator;
    }

    @Override
    public boolean evaluate(EnrolleeSearchContext enrolleeCtx) {
        Term leftTerm = leftTermExtractor.extract(enrolleeCtx);
        Term rightTerm = rightTermExtractor.extract(enrolleeCtx);

        return switch (operator) {
            case EQUALS -> leftTerm.equals(rightTerm);
            case NOT_EQUALS -> !leftTerm.equals(rightTerm);
            case GREATER_THAN -> leftTerm.greaterThan(rightTerm);
            case LESS_THAN -> rightTerm.greaterThan(leftTerm);
            case GREATER_THAN_EQ -> leftTerm.greaterThanOrEqualTo(rightTerm);
            case LESS_THAN_EQ -> rightTerm.greaterThanOrEqualTo(leftTerm);
            default -> throw new IllegalArgumentException("Unknown operator: " + operator);
        };
    }

    @Override
    public EnrolleeSearchQueryBuilder generateQueryBuilder(UUID studyEnvId) {
        EnrolleeSearchQueryBuilder enrolleeSearchQueryBuilder = new EnrolleeSearchQueryBuilder(studyEnvId);

        leftTermExtractor.requiredJoinClauses().forEach(enrolleeSearchQueryBuilder::addJoinClause);
        leftTermExtractor.requiredSelectClauses().forEach(enrolleeSearchQueryBuilder::addSelectClause);
        rightTermExtractor.requiredJoinClauses().forEach(enrolleeSearchQueryBuilder::addJoinClause);
        rightTermExtractor.requiredSelectClauses().forEach(enrolleeSearchQueryBuilder::addSelectClause);


        List<Object> boundObjects = new ArrayList<>();
        boundObjects.addAll(leftTermExtractor.boundObjects());
        boundObjects.addAll(rightTermExtractor.boundObjects());

        Condition whereCondition = condition(
                this.leftTermExtractor.termClause() + " " + this.operator.getOperator() + " " + this.rightTermExtractor.termClause(),
                boundObjects.toArray());

        if (leftTermExtractor.requiredConditions().isPresent()) {
            whereCondition = condition(Operator.AND, leftTermExtractor.requiredConditions().get(), whereCondition);
        }

        if (rightTermExtractor.requiredConditions().isPresent()) {
            whereCondition = condition(Operator.AND, rightTermExtractor.requiredConditions().get(), whereCondition);
        }

        enrolleeSearchQueryBuilder.addCondition(whereCondition);

        return enrolleeSearchQueryBuilder;
    }

}
