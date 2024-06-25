package bio.terra.pearl.core.service.search.terms.functions;

import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import bio.terra.pearl.core.service.search.terms.SearchTerm;
import bio.terra.pearl.core.service.search.terms.SearchValue;
import org.jooq.Condition;

import java.util.List;
import java.util.Optional;

/**
 * Helper class for search functions that can take a variable number of arguments.
 * This class ensures that all arguments are of the same type. Examples: max, min
 */
public abstract class VarArgFunction implements SearchTerm {

    protected final List<SearchTerm> terms;

    public VarArgFunction(List<SearchTerm> terms, SearchValue.SearchValueType valueType) {
        if (terms.stream().anyMatch(term -> term.type().getType() != valueType))
            throw new IllegalArgumentException("All arguments must be of type " + valueType);
        this.terms = terms;
    }

    @Override
    public SearchValue extract(EnrolleeSearchContext enrollee) {
        List<SearchValue> values = this.terms.stream()
                .map(term -> term.extract(enrollee))
                .toList();

        return this.apply(values);
    }

    protected abstract SearchValue apply(List<SearchValue> values);

    @Override
    public List<EnrolleeSearchQueryBuilder.JoinClause> requiredJoinClauses() {
        return this.terms.stream().flatMap(term -> term.requiredJoinClauses().stream()).toList();
    }

    @Override
    public List<EnrolleeSearchQueryBuilder.SelectClause> requiredSelectClauses() {
        return this.terms.stream().flatMap(term -> term.requiredSelectClauses().stream()).toList();
    }

    @Override
    public Optional<Condition> requiredConditions() {
        return this.terms
                .stream()
                .map(SearchTerm::requiredConditions)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce(Condition::and);
    }

    @Override
    public List<Object> boundObjects() {
        return this.terms.stream().flatMap(term -> term.boundObjects().stream()).toList();
    }
}
