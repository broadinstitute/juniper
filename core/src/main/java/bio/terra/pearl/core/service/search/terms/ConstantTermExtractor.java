package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.SQLJoinClause;
import bio.terra.pearl.core.service.search.sql.SQLSelectClause;
import org.jooq.Condition;

import java.util.List;
import java.util.Optional;

public class ConstantTermExtractor implements EnrolleeTermExtractor {

    private final Term term;

    public ConstantTermExtractor(Term term) {
        this.term = term;
    }

    @Override
    public Term extract(EnrolleeSearchContext enrollee) {
        return term;
    }

    @Override
    public List<SQLJoinClause> requiredJoinClauses() {
        return List.of();
    }

    @Override
    public List<SQLSelectClause> requiredSelectClauses() {
        return List.of();
    }

    @Override
    public Optional<Condition> requiredConditions() {
        return Optional.empty();
    }

    @Override
    public List<Object> boundObjects() {
        switch (term.getType()) {
            case STRING -> {
                return List.of(term.getStringValue());
            }
            case INTEGER -> {
                return List.of(term.getIntegerValue());
            }
            case DOUBLE -> {
                return List.of(term.getDoubleValue());
            }
            case BOOLEAN -> {
                return List.of(term.getBooleanValue());
            }
            case INSTANT -> {
                return List.of(term.getInstantValue());
            }
            case DATE -> {
                return List.of(term.getDateValue());
            }
            default -> {
                throw new IllegalArgumentException("Unsupported term type: " + term.getType());
            }
        }
    }

    @Override
    public String termClause() {


        return "?";
    }


}
