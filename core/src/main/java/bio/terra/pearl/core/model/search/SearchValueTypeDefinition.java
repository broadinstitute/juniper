package bio.terra.pearl.core.model.search;

import bio.terra.pearl.core.model.survey.QuestionChoice;
import bio.terra.pearl.core.service.search.terms.SearchValue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class SearchValueTypeDefinition {
    private SearchValue.SearchValueType type;
    private List<QuestionChoice> choices = new ArrayList<>();
    private boolean isMultiValued;
    private boolean hasOtherQuestion;

    public static SearchValueTypeDefinition ofType(SearchValue.SearchValueType type) {
        return SearchValueTypeDefinition.builder()
            .type(type)
            .build();
    }

    public SearchValueTypeDefinition withChoices(List<QuestionChoice> choices) {
        this.choices = choices;
        return this;
    }

    public SearchValueTypeDefinition isMultiValued(boolean isMultiValued) {
        this.isMultiValued = isMultiValued;
        return this;
    }

    public SearchValueTypeDefinition hasOtherQuestion(boolean hasOtherQuestion) {
        this.hasOtherQuestion = hasOtherQuestion;
        return this;
    }
}
