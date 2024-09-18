package bio.terra.pearl.core.model.kit;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Builder;
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
    private UUID collectingAdminUserId;
    private UUID enrolleeId;
    private UUID kitTypeId;
    private KitType kitType;
    @Builder.Default
    private DistributionMethod distributionMethod = DistributionMethod.MAILED; // default to mailed for now
    /**
     * JSON blob of address data sent to DSM, collected from Profile/MailingAddress.
     * In the future, we might decide to store separate fields, or maybe use the postgres jsonb type.
     */
    private String sentToAddress;
    @Builder.Default
    private boolean skipAddressValidation = false;
    private KitRequestStatus status;
    private Instant labeledAt;
    private Instant sentAt;
    private Instant receivedAt;
    private String trackingNumber;
    private String returnTrackingNumber;
    private String kitLabel;  // the barcode 
    private String errorMessage;
    /**
     * JSON blob of the request state from DSM or another sample processor, kept to make sure we capture
     * any fields that don't happen to be stored directly in our model
     */
    private String externalKit;
    private Instant externalKitFetchedAt;
}
