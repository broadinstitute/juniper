package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Collection;
import java.util.UUID;

public interface PepperDSMClient {
    String sendKitRequest(Enrollee enrollee, KitRequest kitRequest, PepperKitAddress address)
            throws JsonProcessingException;
    PepperDSMKitStatus fetchKitStatus(UUID kitRequestId);
    Collection<PepperDSMKitStatus> fetchKitStatusByStudy(UUID studyId);
}
