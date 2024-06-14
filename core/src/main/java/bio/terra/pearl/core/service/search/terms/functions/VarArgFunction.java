package bio.terra.pearl.core.service.search.terms.functions;

import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import bio.terra.pearl.core.service.search.terms.SearchTerm;
import bio.terra.pearl.core.service.search.terms.SearchValue;
import org.jooq.Condition;

import java.util.List;
import java.util.Optional;

public abstract class VarArgFunction implements SearchTerm {

    private final List<SearchTerm> terms;

    public VarArgFunction(SearchTerm term) {
        this.terms = terms;
    }

    @Override
    public SearchValue extract(EnrolleeSearchContext enrollee) {
        SearchValue value = term.extract(enrollee);

        if (value.getSearchValueType() == SearchValue.SearchValueType.STRING) {
            return SearchValue.of(
                    value.getStringValue().toLowerCase(),
                    SearchValue.SearchValueType.STRING);
        }

        throw new IllegalArgumentException("Lower function can only be applied to string values");
    }

    @Override
    public List<EnrolleeSearchQueryBuilder.JoinClause> requiredJoinClauses() {
        return this.term.requiredJoinClauses();
    }

    @Override
    public List<EnrolleeSearchQueryBuilder.SelectClause> requiredSelectClauses() {
        return this.term.requiredSelectClauses();
    }

    @Override
    public Optional<Condition> requiredConditions() {
        return this.term.requiredConditions();
    }

    @Override
    public String termClause() {
        return "LOWER(" + term.termClause() + ")";
    }

    @Override
    public List<Object> boundObjects() {
        return this.term.boundObjects();
    }
}
