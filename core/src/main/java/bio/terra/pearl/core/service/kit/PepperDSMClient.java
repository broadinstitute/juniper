package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;

import java.util.Collection;
import java.util.UUID;

public interface PepperDSMClient {
    /**
     * Sends a sample kit request to Pepper.
     *
     * @param enrollee   the enrollee to receive the sample kit
     * @param kitRequest sample kit request details
     * @param address    mailing address for the sample kit
     * @return status result from Pepper
     * @throws PepperException on error from Pepper or failure to process the Pepper response
     */
    String sendKitRequest(Enrollee enrollee, KitRequest kitRequest, PepperKitAddress address);
    PepperKitStatus fetchKitStatus(UUID kitRequestId);
    Collection<PepperKitStatus> fetchKitStatusByStudy(String pepperStudyName);
}
