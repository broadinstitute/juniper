package bio.terra.pearl.core.model.survey;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ParsedPreRegResponse extends PreregistrationResponse {
    private List<Answer> parsedData;
}
