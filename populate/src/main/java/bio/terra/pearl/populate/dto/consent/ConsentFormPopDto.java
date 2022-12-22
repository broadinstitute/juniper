package bio.terra.pearl.populate.dto.consent;

import bio.terra.pearl.core.model.consent.ConsentForm;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ConsentFormPopDto extends ConsentForm {
    private JsonNode jsonContent;
}
