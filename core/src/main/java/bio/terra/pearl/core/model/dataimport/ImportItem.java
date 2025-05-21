package bio.terra.pearl.core.model.dataimport;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ImportItem extends BaseEntity {
    private UUID createdEnrolleeId;
    private Enrollee createdEnrollee;
    private ParticipantUser createdParticipantUser;
    private UUID createdParticipantUserId;
    private UUID importId;
    private ImportItemStatus status;
    private String message;
    private String detail;

}
