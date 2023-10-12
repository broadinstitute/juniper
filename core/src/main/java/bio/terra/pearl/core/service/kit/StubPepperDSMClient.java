package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.dao.study.StudyEnvironmentDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

@Component
@Slf4j
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
        log.info("STUB sending kit request");
        String fakeResponse = """
                {
                  "kits":[{
                      "error":false,
                      "juniperKitId":"%s",
                      "dsmShippingLabel":"GLMBJIYSQA0XHYV",
                      "participantId":"%2$s",
                      "labelByEmail":"",
                      "scanByEmail":"",
                      "deactivationByEmail":"",
                      "trackingScanBy":"",
                      "errorMessage":"",
                      "discardBy":"",
                      "currentStatus":"Kit Without Label",
                      "collaboratorParticipantId":"PN_%2$s",
                      "collaboratorSampleId":"PN_%2$s_SALIVA_1"
                      }],
                  "isError":false}
                """.formatted(UUID.randomUUID(), enrollee.getShortcode());
        return fakeResponse;
    }

    @Override
    public PepperKitStatus fetchKitStatus(UUID kitRequestId) {
        log.info("STUB fetching kit status");
        return PepperKitStatus.builder()
                .juniperKitId(kitRequestId.toString())
                .currentStatus("SENT")
                .build();
    }

    @Override
    public Collection<PepperKitStatus> fetchKitStatusByStudy(String studyShortcode) {
        log.info("STUB fetching status by study");
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
