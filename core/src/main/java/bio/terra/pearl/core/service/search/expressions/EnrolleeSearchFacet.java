package bio.terra.pearl.core.service.search.expressions;

import bio.terra.pearl.core.service.search.ComparisonOperator;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.SQLSearch;
import bio.terra.pearl.core.service.search.terms.EnrolleeTermExtractor;
import bio.terra.pearl.core.service.search.terms.Term;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

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
    public SQLSearch generateSqlSearch(UUID studyEnvId) {
        DSLContext create = DSL.using(SQLDialect.POSTGRES);

        Query query = create
                .select()
                .from("enrollee")
                .where(leftTermExtractor.termClause().toSql()).sql;

        Condition condition = condition(this.leftTermExtractor., 2);

        return search;
    }

}
