package bio.terra.pearl.core.service.search.expressions;

import bio.terra.pearl.core.dao.participant.EnrolleeDao;
import bio.terra.pearl.core.dao.participant.ProfileDao;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpression;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import bio.terra.pearl.core.service.search.terms.SearchTerm;
import bio.terra.pearl.core.service.search.terms.SearchValue;
import org.jooq.Condition;
import org.jooq.Operator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.jooq.impl.DSL.condition;

/**
 * Search expression that compares two enrollee terms using a comparison operator. This is where the majority
 * of the search logic is implemented.
 */
public class EnrolleeTermComparisonFacet implements EnrolleeSearchExpression {
    private final EnrolleeDao enrolleeDao;
    private final ProfileDao profileDao;
    private final SearchTerm leftTermExtractor;
    private final SearchTerm rightTermExtractor;
    private final SearchOperators operator;

    public EnrolleeTermComparisonFacet(EnrolleeDao enrolleeDao, ProfileDao profileDao, SearchTerm leftTermExtractor, SearchTerm rightTermExtractor, SearchOperators operator) {
        this.leftTermExtractor = leftTermExtractor;
        this.rightTermExtractor = rightTermExtractor;
        this.operator = operator;
        this.enrolleeDao = enrolleeDao;
        this.profileDao = profileDao;
    }

    @Override
    public boolean evaluate(EnrolleeSearchContext enrolleeCtx) {
        SearchValue leftSearchValue = leftTermExtractor.extract(enrolleeCtx);
        SearchValue rightSearchValue = rightTermExtractor.extract(enrolleeCtx);

        return switch (operator) {
            case EQUALS -> leftSearchValue.equals(rightSearchValue);
            case NOT_EQUALS -> !leftSearchValue.equals(rightSearchValue);
            case GREATER_THAN -> leftSearchValue.greaterThan(rightSearchValue);
            case LESS_THAN -> rightSearchValue.greaterThan(leftSearchValue);
            case GREATER_THAN_EQ -> leftSearchValue.greaterThanOrEqualTo(rightSearchValue);
            case LESS_THAN_EQ -> rightSearchValue.greaterThanOrEqualTo(leftSearchValue);
            case CONTAINS -> leftSearchValue.contains(rightSearchValue);
            default -> throw new IllegalArgumentException("Unknown operator: " + operator);
        };
    }

    @Override
    public EnrolleeSearchQueryBuilder generateQueryBuilder(UUID studyEnvId) {
        EnrolleeSearchQueryBuilder enrolleeSearchQueryBuilder = new EnrolleeSearchQueryBuilder(enrolleeDao, profileDao, studyEnvId);

        leftTermExtractor.requiredJoinClauses().forEach(enrolleeSearchQueryBuilder::addJoinClause);
        leftTermExtractor.requiredSelectClauses().forEach(enrolleeSearchQueryBuilder::addSelectClause);
        rightTermExtractor.requiredJoinClauses().forEach(enrolleeSearchQueryBuilder::addJoinClause);
        rightTermExtractor.requiredSelectClauses().forEach(enrolleeSearchQueryBuilder::addSelectClause);


        List<Object> boundObjects = new ArrayList<>();
        boundObjects.addAll(leftTermExtractor.boundObjects());
        boundObjects.addAll(rightTermExtractor.boundObjects());

        Condition whereCondition;
        if (this.operator.equals(SearchOperators.CONTAINS)) {
            // If the operator is CONTAINS, we need to wrap the right term in % to make it a valid SQL LIKE clause
            // contains is case insensitive
            whereCondition = condition(
                    this.leftTermExtractor.termClause() + " ILIKE concat('%', " + rightTermExtractor.termClause() + ", '%')",
                    boundObjects.toArray()
            );
        } else {
            whereCondition = condition(
                    this.leftTermExtractor.termClause() + " " + this.operator.getOperator() + " " + this.rightTermExtractor.termClause(),
                    boundObjects.toArray());

        }

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
