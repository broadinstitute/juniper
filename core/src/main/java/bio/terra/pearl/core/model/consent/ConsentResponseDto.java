package bio.terra.pearl.core.model.consent;

import bio.terra.pearl.core.model.survey.ResponseData;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter @NoArgsConstructor @SuperBuilder
public class ConsentResponseDto extends ConsentResponse {
    private ResponseData parsedData;
}
