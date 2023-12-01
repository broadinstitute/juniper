package bio.terra.pearl.core.service.kit.pepper;

import bio.terra.pearl.core.dao.study.StudyEnvironmentDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.service.kit.KitRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
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
    public PepperKitRequest sendKitRequest(String studyShortcode, Enrollee enrollee, KitRequest kitRequest, PepperKitAddress address) {
        log.info("STUB sending kit request");
        if (address.getCity().startsWith(BAD_ADDRESS_PREFIX) || address.getStreet1().startsWith(BAD_ADDRESS_PREFIX)) {
            throw new  PepperApiException(
                    PEPPER_ADDRESS_VALIDATION_MSG,
                    PepperErrorResponse.builder()
                            .isError(true)
                            .errorMessage(PEPPER_ADDRESS_VALIDATION_MSG)
                            .juniperKitId(kitRequest.getId().toString())
                            .value(new PepperErrorResponse.PepperErrorValue("Address is not valid %s".formatted(kitRequest.getId())))
                            .build(),
                    HttpStatus.BAD_REQUEST);
        }
        PepperKitRequest status = PepperKitRequest.builder()
                .juniperKitId(kitRequest.getId().toString())
                .dsmShippingLabel(UUID.randomUUID().toString())
                .participantId(enrollee.getShortcode())
                .currentStatus("Kit Without Label")
                .build();
        return status;
    }

    @Override
    public PepperKitRequest fetchKitStatus(UUID kitRequestId) {
        log.info("STUB fetching kit status");
        return PepperKitRequest.builder()
                .juniperKitId(kitRequestId.toString())
                .currentStatus("SENT")
                .build();
    }

    @Override
    public Collection<PepperKitRequest> fetchKitStatusByStudy(String studyShortcode) {
        log.info("STUB fetching status by study");
        var studyEnvironment = studyEnvironmentDao.findByStudy(studyShortcode, EnvironmentName.sandbox).get();
        return kitRequestService.findByStudyEnvironment(studyEnvironment.getId()).stream().map(kit -> {
            PepperKitRequest status = PepperKitRequest.builder()
                    .juniperKitId(kit.getId().toString())
                    .currentStatus(getNextStatus(kit))
                    .build();
            return status;
        }).toList();
    }

    protected List<PepperKitStatus> MOCK_STATUS_SEQUENCE = List.of(
            PepperKitStatus.CREATED,
            PepperKitStatus.QUEUED,
            PepperKitStatus.SENT,
            PepperKitStatus.RECEIVED,
            PepperKitStatus.ERRORED,
            PepperKitStatus.DEACTIVATED
    );

    /** helper to get the next status for a kit */
    private String getNextStatus(KitRequest kit) {
        List<String> statusVals = MOCK_STATUS_SEQUENCE.stream().map(status -> status.pepperString).toList();
        try {
            PepperKitRequest pepperStatus = objectMapper.readValue(kit.getExternalRequest(), PepperKitRequest.class);
            String currentStatus = pepperStatus.getCurrentStatus();
            int currentStatusIndex = statusVals.indexOf(currentStatus);
            int nextStatusIndex = (currentStatusIndex + 1) % statusVals.size();
            return statusVals.get(nextStatusIndex);
        } catch (Exception e) {
            return "error";
        }
    }
}
