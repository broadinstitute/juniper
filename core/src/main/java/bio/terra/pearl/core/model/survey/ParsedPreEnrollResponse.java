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
public class ParsedPreEnrollResponse extends PreEnrollmentResponse {
    private List<Answer> parsedData;
}
