package bio.terra.pearl.core.model.audit;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class TraceableEntity extends BaseEntity {
    private UUID responsibleUserId; // id of the user making the change, if it was a participant
    private UUID responsibleAdminUserId; // id of the user making the change, if it was an admin
    private String systemProcess; // if the change was the result of an automatic process, store Class+method here
    private Boolean anonymousUser; // if the change was made by an anonymous user
    private String justification; // if an admin changes a participant's data, a justification is needed

    public void setResponsibleEntity(ResponsibleEntity responsibleEntity) {
        this.responsibleUserId = responsibleEntity.getParticipantUser() != null ? responsibleEntity.getParticipantUser().getId() : null;
        this.responsibleAdminUserId = responsibleEntity.getAdminUser() != null ? responsibleEntity.getAdminUser().getId() : null;
        this.systemProcess = responsibleEntity.getSystemProcess() != null ? responsibleEntity.getSystemProcess() : null;
        this.anonymousUser = responsibleEntity.getAnonymousUser() != null ? responsibleEntity.getAnonymousUser() : null;
    }

}
