package bio.terra.pearl.core.model.participant;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

/** represents a relation between two enrollees.
 * this relationship is directional from the enrollee to the targetEnrollee
 * To read semantically,  "The enrollee with id `enrolleeId` is the `relationshipType` of/to the enrollee with id `targetEnrolleeId`
 * e.g.  if enrolleeId is 1, and targetEnrolleeId is 2, 1 is the proxy of 2. (not vice-versa)
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class EnrolleeRelation extends BaseEntity {
    private UUID enrolleeId;
    private UUID targetEnrolleeId; // note the targetEnrollee does not necessary have to be a subject
    private Enrollee targetEnrollee;
    private Enrollee enrollee;
    private RelationshipType relationshipType;
    private Instant beginDate;
    private Instant endDate;
}
