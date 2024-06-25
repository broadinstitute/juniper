package bio.terra.pearl.core.service.search.terms.functions;

import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import bio.terra.pearl.core.service.search.terms.SearchTerm;
import bio.terra.pearl.core.service.search.terms.SearchValue;
import org.jooq.Condition;

import java.util.List;
import java.util.Optional;

/**
 * A search term that converts a string value to lowercase.
 * Example: lower({enrollee.shortcode}) = 'hdsalk'
 */
public class LowerFunction implements SearchTerm {

    private final SearchTerm term;

    public LowerFunction(SearchTerm term) {
        if (!term.type().getType().equals(SearchValue.SearchValueType.STRING)) {
            throw new IllegalArgumentException("Lower function can only be applied to string values");
        }
        this.term = term;
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

    @Override
    public SearchValueTypeDefinition type() {
        return SearchValueTypeDefinition.builder().type(SearchValue.SearchValueType.STRING).build();
    }
}
