package bio.terra.pearl.core.model.audit;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * DataAuditInfo is a subset of fields that will end up in the DataChangeRecord
 * and are useful for tracking who made changes on what objects.
 * <p>
 * `responsibleUserId` and `responsibleAdminUserId` represent who requested the
 * action and are mutually exclusive. If a user performs on action in the
 * participant ui, then `responsibleUserId` should be filled out; vice versa,
 * if an admin performs an action in the admin ui, `responsibleAdminUserId` should
 * be filled out.
 * <p>
 * The rest of the fields specify relevant objects that the audit should be
 * connected to.
 */
@Getter
@Setter
@SuperBuilder
public class DataAuditInfo {
    private UUID responsibleUserId; // id of the user making the change, if it was a participant
    private UUID responsibleAdminUserId; // id of the user making the change, if it was an admin
    private String systemProcess; // if the change was the result of an automatic process, store Class+method here
    private Boolean anonymousUser; // if the change was made by an anonymous user
    private UUID adminUserId; // if the change is to an admin user, store the id here
    private UUID enrolleeId;
    private UUID portalParticipantUserId;
    private UUID surveyId;
    private UUID familyId;
    private String justification; // if an admin changes a participant's data, a justification is needed

    // If one operation creates multiple DataChangeRecords, then
    // they should all have the same operation id (in other words,
    // you should reuse this object for each)
    @Builder.Default
    private UUID operationId = UUID.randomUUID();

    public static String systemProcessName(Class clazz, String methodName) {
        return "%s.%s".formatted(clazz.getSimpleName(), methodName);
    }

    public void setResponsibleEntity(ResponsibleEntity responsibleEntity) {
        this.responsibleUserId = responsibleEntity.getParticipantUser() != null ? responsibleEntity.getParticipantUser().getId() : null;
        this.responsibleAdminUserId = responsibleEntity.getAdminUser() != null ? responsibleEntity.getAdminUser().getId() : null;
        this.systemProcess = responsibleEntity.getSystemProcess() != null ? responsibleEntity.getSystemProcess() : null;
        this.anonymousUser = responsibleEntity.getAnonymousUser() != null ? responsibleEntity.getAnonymousUser() : null;
    }
}

