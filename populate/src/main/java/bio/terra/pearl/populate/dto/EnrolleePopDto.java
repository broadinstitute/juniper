package bio.terra.pearl.populate.dto;

import bio.terra.pearl.core.model.participant.Enrollee;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class EnrolleePopDto extends Enrollee {
    private String linkedUsername;
}
