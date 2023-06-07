package bio.terra.pearl.core.factory.kit;

import bio.terra.pearl.core.dao.kit.KitRequestDao;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.service.kit.PepperDSMKitStatus;
import bio.terra.pearl.core.service.kit.PepperKitAddress;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class KitRequestFactory {
    private final KitRequestDao kitRequestDao;
    private final ObjectMapper objectMapper;

    public KitRequestFactory(KitRequestDao kitRequestDao,
                             ObjectMapper objectMapper) {
        this.kitRequestDao = kitRequestDao;
        this.objectMapper = objectMapper;
    }

    public KitRequest.KitRequestBuilder builder(String testName) throws JsonProcessingException {
        var address = PepperKitAddress.builder()
                .lastName(testName + "last name")
                .build();
        return KitRequest.builder()
                .sentToAddress(objectMapper.writeValueAsString(address))
                .status(KitRequestStatus.CREATED);
    }

    public KitRequest buildPersisted(String testName, UUID adminUserId, UUID enrolleeId, UUID kitTypeId)
            throws JsonProcessingException {
        var kitRequest = builder(testName)
                .creatingAdminUserId(adminUserId)
                .enrolleeId(enrolleeId)
                .kitTypeId(kitTypeId)
                .build();
        var savedKitRequest = kitRequestDao.create(kitRequest);
        var dsmStatus = PepperDSMKitStatus.builder()
                .currentStatus("CREATED")
                .kitId(savedKitRequest.getId().toString())
                .build();
        savedKitRequest.setDsmStatus(objectMapper.writeValueAsString(dsmStatus));
        savedKitRequest.setDsmStatusFetchedAt(Instant.now());
        return kitRequestDao.update(savedKitRequest);
    }
}
