package bio.terra.pearl.populate.dto.participant;

import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.model.participant.RelationshipType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EnrolleeRelationPopDto extends EnrolleeRelation {
    private String linkedEnrolleeUsername;
    private String enrolleeShortCode;
    private String linkedParticipantUsername;
    private RelationshipType relationshipType;
}
