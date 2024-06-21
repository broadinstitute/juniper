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
public class DataAuditInfo extends TraceableEntity {
    private UUID enrolleeId;
    private UUID portalParticipantUserId;
    private UUID surveyId;

    // If one operation creates multiple DataChangeRecords, then
    // they should all have the same operation id (in other words,
    // you should reuse this object for each)
    @Builder.Default
    private UUID operationId = UUID.randomUUID();

    public static String systemProcessName(Class clazz, String methodName) {
        return "%s.%s".formatted(clazz.getSimpleName(), methodName);
    }
}

