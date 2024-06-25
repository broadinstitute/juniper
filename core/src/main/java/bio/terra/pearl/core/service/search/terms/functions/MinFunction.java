package bio.terra.pearl.core.service.search.terms.functions;

import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import bio.terra.pearl.core.service.search.terms.SearchTerm;
import bio.terra.pearl.core.service.search.terms.SearchValue;

import java.util.List;

/**
 * Selects the minimum value from a list of values.
 * Example: min(1, 2, 3, 4) = 1
 */
public class MinFunction extends VarArgFunction {
    public MinFunction(List<SearchTerm> terms) {
        super(terms, SearchValue.SearchValueType.NUMBER);
    }

    @Override
    protected SearchValue apply(List<SearchValue> values) {
        double min = values.stream()
                .map(SearchValue::getNumberValue)
                .reduce((double) Double.MAX_VALUE, Math::min);

        return new SearchValue(min);
    }

    @Override
    public String termClause() {
        return String.format("LEAST(%s)", String.join(", ", this.terms.stream().map(SearchTerm::termClause).toList()));
    }

    @Override
    public SearchValueTypeDefinition type() {
        return SearchValueTypeDefinition.builder().type(SearchValue.SearchValueType.NUMBER).build();
    }
}
