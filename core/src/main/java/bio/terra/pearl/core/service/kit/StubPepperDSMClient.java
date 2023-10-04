package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.dao.study.StudyEnvironmentDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

@Component
public class StubPepperDSMClient implements PepperDSMClient {
    private static final Logger logger = LoggerFactory.getLogger(PepperDSMClient.class);
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
        logger.info("STUB sending kit request");
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
        logger.info("STUB fethcing kit status");
        return PepperKitStatus.builder()
                .juniperKitId(kitRequestId.toString())
                .currentStatus("SENT")
                .build();
    }

    @Override
    public Collection<PepperKitStatus> fetchKitStatusByStudy(String studyShortcode) {
        logger.info("STUB fetching status by study");
        var studyEnvironment = studyEnvironmentDao.findByStudy(studyShortcode, EnvironmentName.sandbox).get();
        return kitRequestService.findIncompleteKits(studyEnvironment.getId()).stream().map(kit -> {
            PepperKitStatus status = PepperKitStatus.builder()
                    .juniperKitId(kit.getId().toString())
                    .currentStatus("SENT")
                    .build();
            return status;
        }).toList();
    }
}
