package bio.terra.pearl.core.model.audit;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * logs a discrete change to a participant's persisted data, such as a change to their profile or a survey
 * This is mainly kept for auditing (HIPAA) and troubleshooting purposes.
 * To support application-level functionality (such as undo/redo)
 * more sophisticated mechanisms should likely be used.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ParticipantDataChange extends BaseEntity implements DataChange {
    private UUID portalEnvironmentId; // id of the associated portal, for operations not tied to an enrollee
    private UUID responsibleUserId; // id of the user making the change, if it was a participant
    private UUID responsibleAdminUserId; // id of the user making the change, if it was an admin
    private String systemProcess; // if the change was the result of an automatic process, store Class+method here
    private Boolean anonymousUser; // if the change was made by an anonymous user
    private UUID enrolleeId; // id of impacted enrollee (may be null)
    private UUID portalParticipantUserId; // id of the impacted portal participant user
    private UUID operationId; // unique id to group operations
    private UUID surveyId; // survey id of the form source of the change
    private UUID familyId; // family id related to this change
    private UUID modelId; // id of the object corresponding to the audit record
    private String modelName; // either a class (like Profile) or a stableId of a survey
    private String fieldName; // either a property of a class (like givenName) or a survey question stableId
    private String justification; // if an admin changes a participant's responses, a justification is needed
    private String oldValue;
    private String newValue;

    public static ParticipantDataChange.ParticipantDataChangeBuilder fromAuditInfo(DataAuditInfo auditInfo) {
        return ParticipantDataChange.builder()
                .responsibleAdminUserId(auditInfo.getResponsibleAdminUserId())
                .responsibleUserId(auditInfo.getResponsibleUserId())
                .systemProcess(auditInfo.getSystemProcess())
                .anonymousUser(auditInfo.getAnonymousUser())
                .operationId(auditInfo.getOperationId())
                .enrolleeId(auditInfo.getEnrolleeId())
                .portalParticipantUserId(auditInfo.getPortalParticipantUserId())
                .surveyId(auditInfo.getSurveyId())
                .familyId(auditInfo.getFamilyId())
                .justification(auditInfo.getJustification());
    }
}
