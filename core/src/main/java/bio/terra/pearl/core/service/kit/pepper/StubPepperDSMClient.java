package bio.terra.pearl.core.service.kit.pepper;

import bio.terra.pearl.core.dao.study.StudyEnvironmentDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.service.kit.KitRequestService;
import bio.terra.pearl.core.service.kit.pepper.PepperDSMClient;
import bio.terra.pearl.core.service.kit.pepper.PepperKitAddress;
import bio.terra.pearl.core.service.kit.pepper.PepperKitStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

@Component
@Slf4j
public class StubPepperDSMClient implements PepperDSMClient {
    private final KitRequestService kitRequestService;
    private final StudyEnvironmentDao studyEnvironmentDao;
    private final ObjectMapper objectMapper;

    private final static String BAD_ADDRESS_PREFIX = "BAD";
    private final static String PEPPER_ADDRESS_VALIDATION_MSG = "ADDRESS_VALIDATION_ERROR";

    public StubPepperDSMClient(@Lazy KitRequestService kitRequestService,
                               StudyEnvironmentDao studyEnvironmentDao,
                               ObjectMapper objectMapper) {
        this.kitRequestService = kitRequestService;
        this.studyEnvironmentDao = studyEnvironmentDao;
        this.objectMapper = objectMapper;
    }

    @Override
    public PepperKitStatus sendKitRequest(String studyShortcode, Enrollee enrollee, KitRequest kitRequest, PepperKitAddress address) {
        log.info("STUB sending kit request");
        if (address.getCity().startsWith(BAD_ADDRESS_PREFIX) || address.getStreet1().startsWith(BAD_ADDRESS_PREFIX)) {
            throw new  PepperApiException(
                    "Error from Pepper",
                    PepperErrorResponse.builder()
                            .isError(true)
                            .errorMessage(PEPPER_ADDRESS_VALIDATION_MSG)
                            .juniperKitId(kitRequest.getId().toString())
                            .value(new PepperErrorResponse.PepperErrorValue("Address is not valid %s".formatted(kitRequest.getId())))
                            .build(),
                    HttpStatus.BAD_REQUEST);
        }
        PepperKitStatus status = PepperKitStatus.builder()
                .juniperKitId(kitRequest.getId().toString())
                .dsmShippingLabel(UUID.randomUUID().toString())
                .participantId(enrollee.getShortcode())
                .currentStatus("Kit Without Label")
                .build();
        return status;
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
