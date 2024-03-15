package bio.terra.pearl.core.service.search.expressions;

import bio.terra.pearl.core.service.search.ComparisonOperator;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.SQLSearch;
import bio.terra.pearl.core.service.search.sql.SQLWhereComparisonExpression;
import bio.terra.pearl.core.service.search.terms.EnrolleeTermExtractor;
import bio.terra.pearl.core.service.search.terms.Term;

import java.util.UUID;

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
    public SQLSearch generateSqlSearch(UUID studyEnvId) {
        SQLSearch search = new SQLSearch(studyEnvId);
        leftTermExtractor.requiredJoinClauses().forEach(search::addJoinClause);
        leftTermExtractor.requiredSelectClauses().forEach(search::addSelectClause);
        rightTermExtractor.requiredJoinClauses().forEach(search::addJoinClause);
        rightTermExtractor.requiredSelectClauses().forEach(search::addSelectClause);

        search.setSqlWhereClause(new SQLWhereComparisonExpression(
                leftTermExtractor.termClause(),
                rightTermExtractor.termClause(),
                operator
        ));

        return search;
    }

}
