package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.dao.study.StudyEnvironmentDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Component
public class StubPepperDSMClient implements PepperDSMClient {
    private final KitRequestService kitRequestService;
    private final StudyEnvironmentDao studyEnvironmentDao;
    private final ObjectMapper objectMapper;

    public StubPepperDSMClient(@Lazy KitRequestService kitRequestService,
                               StudyEnvironmentDao studyEnvironmentDao,
                               ObjectMapper objectMapper) {
        this.kitRequestService = kitRequestService;
        this.studyEnvironmentDao = studyEnvironmentDao;
        this.objectMapper = objectMapper;
    }

    @Override
    public JsonNode sendKitRequest(String studyShortcode, Enrollee enrollee, KitRequest kitRequest, PepperKitAddress address) {
        PepperKitStatus kitStatus = PepperKitStatus.builder()
                .juniperKitId(kitRequest.getId().toString())
                .currentStatus("CREATED").build();
        var response = PepperKitStatusResponse.builder()
                .kits(new PepperKitStatus[] {kitStatus})
                .isError(false).build();
        return objectMapper.valueToTree(response);
    }

    @Override
    public JsonNode fetchKitStatus(UUID kitRequestId) {
        PepperKitStatus kitStatus = PepperKitStatus.builder()
                .juniperKitId(kitRequestId.toString())
                .currentStatus("SHIPPED")
                .build();
        var response = PepperKitStatusResponse.builder()
                .kits(new PepperKitStatus[] { kitStatus })
                .isError(false)
                .build();
        return objectMapper.valueToTree(response);
    }

    @Override
    public JsonNode fetchKitStatusByStudy(String studyShortcode) {
        var studyEnvironment = studyEnvironmentDao.findByStudy(studyShortcode, EnvironmentName.sandbox).get();
        var kits = kitRequestService.findIncompleteKits(studyEnvironment.getId()).stream().map(kit -> {
            return PepperKitStatus.builder()
                    .juniperKitId(kit.getId().toString())
                    .currentStatus("SHIPPED")
                    .build();
        }).toList();
        var response = PepperKitStatusResponse.builder()
                .kits(kits.toArray(new PepperKitStatus[0]))
                .isError(false)
                .build();
        return objectMapper.valueToTree(response);
    }
}
