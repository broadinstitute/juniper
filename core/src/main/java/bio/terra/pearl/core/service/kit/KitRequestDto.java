package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.model.kit.DistributionMethod;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.service.kit.pepper.PepperKit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * Holds set of kit request data that is relevant to users
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Slf4j
public class KitRequestDto {
  private UUID id;
  private UUID creatingAdminUserId;
  private UUID collectingAdminUserId;
  private Instant createdAt;
  private KitType kitType;
  private DistributionMethod distributionMethod;
  private KitRequestStatus status;
  private String sentToAddress;
  private Instant labeledAt;
  private Instant sentAt;
  private Instant receivedAt;
  private String trackingNumber;
  private String returnTrackingNumber;
  private String kitLabel;   // the barcode from the kit label
  private boolean skipAddressValidation;
  private String errorMessage;
  /**
   * JSON blob for additional kit request details
   */
  private String details;
  private String enrolleeShortcode;

  public KitRequestDto(KitRequest kitRequest, KitType kitType, String enrolleeShortcode,
                       ObjectMapper objectMapper) {
    this.id = kitRequest.getId();
    this.creatingAdminUserId = kitRequest.getCreatingAdminUserId();
    this.collectingAdminUserId = kitRequest.getCollectingAdminUserId();
    this.createdAt = kitRequest.getCreatedAt();
    this.kitType = kitType;
    this.distributionMethod = kitRequest.getDistributionMethod();
    this.sentToAddress = kitRequest.getSentToAddress();
    this.status = kitRequest.getStatus();
    this.labeledAt = kitRequest.getLabeledAt();
    this.sentAt = kitRequest.getSentAt();
    this.receivedAt = kitRequest.getReceivedAt();
    this.trackingNumber = kitRequest.getTrackingNumber();
    this.returnTrackingNumber = kitRequest.getReturnTrackingNumber();
    this.kitLabel = kitRequest.getKitLabel();
    this.errorMessage = kitRequest.getErrorMessage();
    this.skipAddressValidation = kitRequest.isSkipAddressValidation();
    this.details = createRequestDetails(kitRequest, objectMapper);
    this.enrolleeShortcode = enrolleeShortcode;
  }

  protected static String createRequestDetails(KitRequest kitRequest, ObjectMapper objectMapper) {
    if(kitRequest.getDistributionMethod() != DistributionMethod.MAILED) {
      return null;
    }

    ObjectNode rootNode = objectMapper.createObjectNode();
    rootNode.put("requestId", kitRequest.getId().toString());
    try {
      PepperKit pepperKit = objectMapper.readValue(kitRequest.getExternalKit(), PepperKit.class);
      rootNode.put("shippingId", pepperKit.getDsmShippingLabel());
      if (pepperKit.getErrorDate() != null) {
        rootNode.put("errorDate", pepperKit.getErrorDate());
      }
      if (pepperKit.getDeactivationReason() != null) {
        rootNode.put("deactivationReason", pepperKit.getDeactivationReason());
      }
      if (pepperKit.getDeactivationDate() != null) {
        rootNode.put("deactivationDate", pepperKit.getDeactivationDate());
      }
      return objectMapper.writeValueAsString(rootNode);
    } catch (Exception e) {
      // PepperKit was already parsed, so this should never happen, but just in case
      log.error("Could not parse JSON kit status response from DSM (PepperKit)", e);
      return null;
    }
  }
}
