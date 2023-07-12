package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.dao.kit.KitRequestDao;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

@Component
public class StubPepperDSMClient implements PepperDSMClient {
    private final KitRequestDao kitRequestDao;
    private final ObjectMapper objectMapper;

    public StubPepperDSMClient(KitRequestDao kitRequestDao,
                               ObjectMapper objectMapper) {
        this.kitRequestDao = kitRequestDao;
        this.objectMapper = objectMapper;
    }

    @Override
    public String sendKitRequest(Enrollee enrollee, KitRequest kitRequest, PepperKitAddress address) {
        var statusBuilder = PepperKitStatus.builder()
                .kitId(kitRequest.getId().toString())
                .currentStatus("CREATED");
        try {
            return objectMapper.writeValueAsString(statusBuilder.build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PepperKitStatus fetchKitStatus(UUID kitRequestId) {
        return PepperKitStatus.builder()
                .kitId(kitRequestId.toString())
                .currentStatus("SHIPPED")
                .build();
    }

    @Override
    public Collection<PepperKitStatus> fetchKitStatusByStudy(UUID studyEnvironmentId) {
        return kitRequestDao.findIncompleteKits(studyEnvironmentId).stream().map(kit -> {
            PepperKitStatus status = PepperKitStatus.builder()
                    .kitId(kit.getId().toString())
                    .currentStatus("SHIPPED")
                    .build();
            return status;
        }).toList();
    }
}
