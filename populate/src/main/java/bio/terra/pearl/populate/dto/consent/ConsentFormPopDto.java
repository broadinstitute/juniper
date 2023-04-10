package bio.terra.pearl.populate.dto.consent;

import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.populate.dto.FilePopulatable;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ConsentFormPopDto extends ConsentForm implements FilePopulatable {
    private JsonNode jsonContent;
    private String populateFileName;
}
