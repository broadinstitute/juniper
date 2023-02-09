package bio.terra.pearl.populate.dto.survey;

import bio.terra.pearl.core.model.survey.PreEnrollmentResponse;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class PreEnrollmentResponsePopDto extends PreEnrollmentResponse {
    private String surveyStableId;
    private int surveyVersion;
    private JsonNode fullDataJson;
}
