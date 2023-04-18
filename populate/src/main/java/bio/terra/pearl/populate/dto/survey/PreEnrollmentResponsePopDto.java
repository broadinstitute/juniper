package bio.terra.pearl.populate.dto.survey;

import bio.terra.pearl.core.model.survey.PreEnrollmentResponse;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class PreEnrollmentResponsePopDto extends PreEnrollmentResponse {
    private String surveyStableId;
    private int surveyVersion;
    List<AnswerPopDto> answers;
}
