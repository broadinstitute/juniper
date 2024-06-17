package bio.terra.pearl.core.service.search.terms.functions;

import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import bio.terra.pearl.core.service.search.terms.SearchTerm;
import bio.terra.pearl.core.service.search.terms.SearchValue;

import java.util.List;

public class MaxFunction extends VarArgFunction {
    public MaxFunction(List<SearchTerm> terms) {
        super(terms, SearchValue.SearchValueType.NUMBER);
    }

    @Override
    protected SearchValue apply(List<SearchValue> values) {
        double min = values.stream()
                .map(SearchValue::getNumberValue)
                .reduce((double) Double.MIN_VALUE, Math::max);

        return new SearchValue(min);
    }

    @Override
    public String termClause() {
        return String.format("GREATEST(%s)", String.join(", ", this.terms.stream().map(SearchTerm::termClause).toList()));
    }

    @Override
    public SearchValueTypeDefinition type() {
        return SearchValueTypeDefinition.builder().type(SearchValue.SearchValueType.NUMBER).build();
    }
}
