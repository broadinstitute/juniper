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
    private final TimeComparisonType timeComparisonType;

    public EnrolleeTermComparisonFacet(EnrolleeDao enrolleeDao, ProfileDao profileDao, SearchTerm leftTermExtractor, SearchTerm rightTermExtractor, SearchOperators operator) {
        this.leftTermExtractor = leftTermExtractor;
        this.rightTermExtractor = rightTermExtractor;
        this.operator = operator;
        this.enrolleeDao = enrolleeDao;
        this.profileDao = profileDao;
        this.timeComparisonType = getTimeComparisonType();
    }

    @Override
    public boolean evaluate(EnrolleeSearchContext enrolleeCtx) {
        SearchValue leftSearchValue = leftTermExtractor.extract(enrolleeCtx);
        SearchValue rightSearchValue = rightTermExtractor.extract(enrolleeCtx);

        if (timeComparisonType.equals(TimeComparisonType.TEMPORAL_TO_STRING)) {
            // If the left value is an instant/date and the right value is a string, we need to parse the right value to an instant
            rightSearchValue.parseTo(leftSearchValue.getSearchValueType());
        } else if (timeComparisonType.equals(TimeComparisonType.STRING_TO_TEMPORAL)) {
            // If the right value is an instant/date and the left value is a string, we need to parse the right value to an instant
            leftSearchValue.parseTo(rightSearchValue.getSearchValueType());
        }

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
            whereCondition = createContainsCondition(boundObjects);
        } else if (requiresTemporalCast()) {
            whereCondition = createConditionWithTemporalCast(boundObjects);
        } else if (isNullComparison()) {
            whereCondition = createNullComparisonCondition(boundObjects);
        } else {
            // for simple comparisons, use terms and operator directly
            whereCondition = condition(
                        "%s %s %s".formatted(leftTermExtractor.termClause(), this.operator.getOperator(), this.rightTermExtractor.termClause()),
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

    private Condition createContainsCondition(List<Object> boundObjects) {
        // If the operator is CONTAINS, we need to wrap the right term in % to make it a valid SQL LIKE clause
        // contains is case insensitive
        return condition(
                this.leftTermExtractor.termClause() + " ILIKE concat('%', " + rightTermExtractor.termClause() + ", '%')",
                boundObjects.toArray());
    }

    private boolean requiresTemporalCast() {
        return !this.timeComparisonType.equals(TimeComparisonType.OTHER);
    }

    private Condition createConditionWithTemporalCast(List<Object> boundObjects) {
        return switch (timeComparisonType) {
            case TEMPORAL_TO_STRING -> {
                String castType = leftTermExtractor.type().getType().equals(SearchValue.SearchValueType.INSTANT) ? "timestamp" : "date";
                yield condition(
                        "%s %s %s::%s".formatted(this.leftTermExtractor.termClause(), this.operator.getOperator(), this.rightTermExtractor.termClause(), castType),
                        boundObjects.toArray());
            }
            case STRING_TO_TEMPORAL -> {
                String castType = rightTermExtractor.type().getType().equals(SearchValue.SearchValueType.INSTANT) ? "timestamp" : "date";
                yield condition(
                        "%s::%s %s %s".formatted(this.leftTermExtractor.termClause(), castType, this.operator.getOperator(), this.rightTermExtractor.termClause()),
                        boundObjects.toArray());
            }
            case OTHER -> throw new IllegalArgumentException("no temporal cast required for this comparison type");
        };
    }

    private boolean isNullComparison() {
        return this.rightTermExtractor.type().getType().equals(SearchValue.SearchValueType.NULL) &&
                (this.operator.equals(SearchOperators.EQUALS) || this.operator.equals(SearchOperators.NOT_EQUALS));
    }

    private Condition createNullComparisonCondition(List<Object> boundObjects) {
        return switch (this.operator) {
            case EQUALS -> condition("%s IS NULL".formatted(leftTermExtractor.termClause()), boundObjects.toArray());
            case NOT_EQUALS ->
                    condition("%s IS NOT NULL".formatted(leftTermExtractor.termClause()), boundObjects.toArray());
            default ->
                    throw new IllegalArgumentException("Cannot use operator %s with NULL value".formatted(this.operator));
        };

    }

    /** We need to do special-case parsing and casting for time comparison, to handle string -> time conversions */
    private TimeComparisonType getTimeComparisonType() {
        if ((leftTermExtractor.type().getType().equals(SearchValue.SearchValueType.INSTANT) || leftTermExtractor.type().getType().equals(SearchValue.SearchValueType.DATE)) &&
                rightTermExtractor.type().getType().equals(SearchValue.SearchValueType.STRING)) {
            return TimeComparisonType.TEMPORAL_TO_STRING;
        } else if ((rightTermExtractor.type().getType().equals(SearchValue.SearchValueType.INSTANT) || rightTermExtractor.type().getType().equals(SearchValue.SearchValueType.DATE)) &&
                leftTermExtractor.type().getType().equals(SearchValue.SearchValueType.STRING)) {
            return TimeComparisonType.STRING_TO_TEMPORAL;
        } else {
            return TimeComparisonType.OTHER;
        }
    }

    private enum TimeComparisonType {
        TEMPORAL_TO_STRING,
        STRING_TO_TEMPORAL,
        OTHER
    }

}
