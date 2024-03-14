package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.SQLJoinClause;
import bio.terra.pearl.core.service.search.sql.SQLSelectClause;
import bio.terra.pearl.core.service.search.sql.SQLWhereClause;
import bio.terra.pearl.core.service.search.sql.SQLWhereValue;

import java.util.List;

public class ConstantTermExtractor implements EnrolleeTermExtractor{

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
    public SQLWhereClause termClause() {
        String value = switch (term.getType()) {
            case BOOLEAN -> term.getBooleanValue().toString();
            case STRING -> term.getStringValue();
            case INTEGER -> term.getIntegerValue().toString();
            case DOUBLE -> term.getDoubleValue().toString();
            case INSTANT -> term.getInstantValue().toString();
            default -> "";
        };

        return new SQLWhereValue(value);
    }


}
