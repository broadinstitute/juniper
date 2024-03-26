package bio.terra.pearl.core.model.participant;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * EnrolleeRelationDto is a data transfer object that contains the EnrolleeRelation and the Profile
 * The profile is the profile of the targetEnrollee in the relation (for example the Governed user)
 *The profile of the enrollee (for example the user who is the parent of the Governed user) is not included in this DTO, and is being send
 * separately in the response.
 * */
@SuperBuilder
@Getter @Setter
public class EnrolleeRelationDto {
    EnrolleeRelation relation;
    Profile profile;
}
