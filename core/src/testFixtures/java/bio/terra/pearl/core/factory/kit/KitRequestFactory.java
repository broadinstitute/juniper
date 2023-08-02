package bio.terra.pearl.core.factory.kit;

import bio.terra.pearl.core.dao.kit.KitRequestDao;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.service.kit.PepperKitStatus;
import bio.terra.pearl.core.service.kit.PepperKitAddress;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KitRequestFactory {
    @Autowired
    private KitRequestDao kitRequestDao;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private KitTypeFactory kitTypeFactory;
    @Autowired
    private AdminUserFactory adminUserFactory;


    public KitRequest.KitRequestBuilder builder(String testName) throws JsonProcessingException {
        var address = PepperKitAddress.builder()
                .lastName(testName + "last name")
                .build();
        return KitRequest.builder()
                .sentToAddress(objectMapper.writeValueAsString(address))
                .status(KitRequestStatus.CREATED);
    }

    public KitRequest buildPersisted(String testName, UUID enrolleeId) throws JsonProcessingException {
        KitType kitType = kitTypeFactory.buildPersisted(testName);
        return buildPersisted(testName, enrolleeId, kitType.getId());
    }

    public KitRequest buildPersisted(String testName, UUID enrolleeId, UUID kitTypeId) throws JsonProcessingException {
        AdminUser adminUser = adminUserFactory.buildPersisted(testName);
        return buildPersisted(testName, enrolleeId, kitTypeId, adminUser.getId());
    }

    public KitRequest buildPersisted(String testName, UUID enrolleeId, UUID kitTypeId, UUID adminUserId)
            throws JsonProcessingException {
        var kitRequest = builder(testName)
                .creatingAdminUserId(adminUserId)
                .enrolleeId(enrolleeId)
                .kitTypeId(kitTypeId)
                .build();
        var savedKitRequest = kitRequestDao.create(kitRequest);
        var dsmStatus = PepperKitStatus.builder()
                .currentStatus("CREATED")
                .kitId(savedKitRequest.getId().toString())
                .build();
        savedKitRequest.setDsmStatus(objectMapper.writeValueAsString(dsmStatus));
        savedKitRequest.setDsmStatusFetchedAt(Instant.now());
        return kitRequestDao.update(savedKitRequest);
    }
}
