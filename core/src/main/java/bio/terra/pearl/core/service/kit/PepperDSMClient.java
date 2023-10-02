package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collection;
import java.util.UUID;

public interface PepperDSMClient {
    /**
     * Sends a sample kit request to Pepper.
     *
     * @param studyShortcode    the shortcode of the Juniper study
     * @param enrollee          the enrollee to receive the sample kit
     * @param kitRequest        sample kit request details
     * @param address           mailing address for the sample kit
     * @return status result from Pepper
     * @throws PepperException on error from Pepper or failure to process the Pepper response
     */
    JsonNode sendKitRequest(String studyShortcode, Enrollee enrollee, KitRequest kitRequest, PepperKitAddress address);
    JsonNode fetchKitStatus(UUID kitRequestId);
    JsonNode fetchKitStatusByStudy(String studyShortcode);
}
