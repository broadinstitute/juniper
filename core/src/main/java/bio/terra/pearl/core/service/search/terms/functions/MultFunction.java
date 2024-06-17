package bio.terra.pearl.core.service.search.terms.functions;

import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import bio.terra.pearl.core.service.search.terms.SearchTerm;
import bio.terra.pearl.core.service.search.terms.SearchValue;

import java.util.List;

public class MultFunction extends VarArgFunction {
    public MultFunction(List<SearchTerm> terms) {
        super(terms, SearchValue.SearchValueType.NUMBER);
    }

    @Override
    protected SearchValue apply(List<SearchValue> values) {
        double sum = values.stream()
                .map(SearchValue::getNumberValue)
                .reduce((double) 1, (a, b) -> a * b);

        return new SearchValue(sum);
    }

    @Override
    public String termClause() {
        return String.format("(%s)", String.join(" * ", this.terms.stream().map(SearchTerm::termClause).toList()));
    }

    @Override
    public SearchValueTypeDefinition type() {
        return SearchValueTypeDefinition.builder().type(SearchValue.SearchValueType.NUMBER).build();
    }
}