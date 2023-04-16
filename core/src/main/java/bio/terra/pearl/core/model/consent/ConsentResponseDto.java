package bio.terra.pearl.core.model.consent;

import bio.terra.pearl.core.model.survey.Answer;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter @NoArgsConstructor @SuperBuilder
public class ConsentResponseDto extends ConsentResponse {
    private List<Answer> answers;
}
