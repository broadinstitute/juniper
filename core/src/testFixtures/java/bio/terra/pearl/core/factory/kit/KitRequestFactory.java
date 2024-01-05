package bio.terra.pearl.core.factory.kit;

import bio.terra.pearl.core.dao.kit.KitRequestDao;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.service.kit.pepper.PepperKit;
import bio.terra.pearl.core.service.kit.pepper.PepperKitAddress;
import bio.terra.pearl.core.service.kit.pepper.PepperKitStatus;
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

    public KitRequest buildPersisted(String testName, Enrollee enrollee) throws JsonProcessingException {
        KitType kitType = kitTypeFactory.buildPersisted(testName);
        var kitRequest = buildPersisted(testName, enrollee, PepperKitStatus.CREATED, kitType.getId());
        kitRequest.setKitType(kitType);
        return kitRequest;
    }

    public KitRequest buildPersisted(String testName, Enrollee enrollee, PepperKitStatus pepperKitStatus) throws JsonProcessingException {
        KitType kitType = kitTypeFactory.buildPersisted(testName);
        var kitRequest = buildPersisted(testName, enrollee, pepperKitStatus, kitType.getId());
        kitRequest.setKitType(kitType);
        return kitRequest;
    }

    public KitRequest buildPersisted(String testName, Enrollee enrollee, PepperKitStatus pepperKitStatus,
                                     UUID kitTypeId) throws JsonProcessingException {
        AdminUser adminUser = adminUserFactory.buildPersisted(testName);
        return buildPersisted(testName, enrollee, pepperKitStatus, kitTypeId, adminUser.getId());
    }

    public KitRequest buildPersisted(String testName, Enrollee enrollee, PepperKitStatus pepperKitStatus,
                                     UUID kitTypeId, UUID adminUserId)
        throws JsonProcessingException {
        var kitRequest = builder(testName)
            .creatingAdminUserId(adminUserId)
            .enrolleeId(enrollee.getId())
            .kitTypeId(kitTypeId)
            .status(PepperKitStatus.mapToKitRequestStatus(pepperKitStatus.pepperString))
            .build();
        KitRequest savedKitRequest = kitRequestDao.create(kitRequest);
        PepperKit dsmStatus = PepperKit.builder()
            .dsmShippingLabel(UUID.randomUUID().toString())
            .currentStatus(pepperKitStatus.pepperString)
            .juniperKitId(savedKitRequest.getId().toString())
            .participantId(enrollee.getShortcode())
            .build();
        savedKitRequest.setExternalKit(objectMapper.writeValueAsString(dsmStatus));
        savedKitRequest.setExternalKitFetchedAt(Instant.now());
        return kitRequestDao.update(savedKitRequest);
    }
}
