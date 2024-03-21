package bio.terra.pearl.core.model.participant;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter @Setter
public class EnrolleeRelationDto {
    EnrolleeRelation relation;
    Profile profile;
}
