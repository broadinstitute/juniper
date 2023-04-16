package bio.terra.pearl.populate.dto.consent;

import bio.terra.pearl.core.model.consent.ConsentResponseDto;
import bio.terra.pearl.populate.dto.survey.AnswerPopDto;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class ConsentResponsePopDto extends ConsentResponseDto {
    private String consentStableId;
    private int consentVersion;
    private List<AnswerPopDto> answerPopDtos;
}
