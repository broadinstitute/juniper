package bio.terra.pearl.core.model.search;

import bio.terra.pearl.core.model.survey.QuestionChoice;
import bio.terra.pearl.core.service.search.terms.SearchValue;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode
public class SearchValueTypeDefinition {
    private SearchValue.SearchValueType type;
    @Builder.Default
    private List<QuestionChoice> choices = new ArrayList<>();
    private boolean isMultiValued;
    private boolean hasOtherQuestion;
}
