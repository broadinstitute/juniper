package bio.terra.pearl.populate.dto.survey;

import bio.terra.pearl.core.model.survey.Survey;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class SurveyPopDto extends Survey {
    private JsonNode jsonContent;
}
