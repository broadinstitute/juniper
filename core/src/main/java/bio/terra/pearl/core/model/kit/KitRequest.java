package bio.terra.pearl.core.model.kit;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.service.kit.PepperKitStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter @SuperBuilder
@NoArgsConstructor
public class KitRequest extends BaseEntity {
    private UUID creatingAdminUserId;
    private UUID enrolleeId;
    private Enrollee enrollee;
    private UUID kitTypeId;
    private KitType kitType;
    /**
     * JSON blob of address data sent to DSM, collected from Profile/MailingAddress.
     * In the future, we might decide to store separate fields, or maybe use the postgres jsonb type.
     */
    private String sentToAddress;
    /**
     * Status of the Juniper kit request. Since the Juniper and Pepper entities have related but separate lifecycles,
     * we may need to track separate status. This will help Juniper know whether the kit is waiting on something to
     * happen in Pepper or if action is needed on the Juniper side.
     */
    private KitRequestStatus status;
    /**
     * JSON blob of status from DSM
     */
    private String dsmStatus;
    private PepperKitStatus pepperStatus;
    private Instant dsmStatusFetchedAt;
}
