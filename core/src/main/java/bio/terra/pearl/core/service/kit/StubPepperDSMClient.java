package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.dao.study.StudyEnvironmentDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Collection;
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
    public String sendKitRequest(String studyShortcode, Enrollee enrollee, KitRequest kitRequest, PepperKitAddress address) {
        var statusBuilder = PepperKitStatus.builder()
                .juniperKitId(kitRequest.getId().toString())
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
                .juniperKitId(kitRequestId.toString())
                .currentStatus("SHIPPED")
                .build();
    }

    @Override
    public Collection<PepperKitStatus> fetchKitStatusByStudy(String studyShortcode) {
        var studyEnvironment = studyEnvironmentDao.findByStudy(studyShortcode, EnvironmentName.sandbox).get();
        return kitRequestService.findIncompleteKits(studyEnvironment.getId()).stream().map(kit -> {
            PepperKitStatus status = PepperKitStatus.builder()
                    .juniperKitId(kit.getId().toString())
                    .currentStatus("SHIPPED")
                    .build();
            return status;
        }).toList();
    }
}
