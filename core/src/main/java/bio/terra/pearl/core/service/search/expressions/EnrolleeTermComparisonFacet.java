package bio.terra.pearl.core.service.search.expressions;

import bio.terra.pearl.core.dao.participant.EnrolleeDao;
import bio.terra.pearl.core.dao.participant.ProfileDao;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpression;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import bio.terra.pearl.core.service.search.terms.EnrolleeTerm;
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
    private final EnrolleeTerm leftTermExtractor;
    private final EnrolleeTerm rightTermExtractor;
    private final ComparisonOperator operator;

    public EnrolleeTermComparisonFacet(EnrolleeDao enrolleeDao, ProfileDao profileDao, EnrolleeTerm leftTermExtractor, EnrolleeTerm rightTermExtractor, ComparisonOperator operator) {
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
