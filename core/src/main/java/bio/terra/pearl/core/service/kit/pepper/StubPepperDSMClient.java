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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
    public PepperKit sendKitRequest(String studyShortcode, Enrollee enrollee, KitRequest kitRequest, PepperKitAddress address) {
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
        PepperKit status = PepperKit.builder()
                .juniperKitId(kitRequest.getId().toString())
                .dsmShippingLabel(UUID.randomUUID().toString())
                .participantId(enrollee.getShortcode())
                .currentStatus("Kit Without Label")
                .build();
        return status;
    }

    @Override
    public PepperKit fetchKitStatus(UUID kitRequestId) {
        log.info("STUB fetching kit status");
        return PepperKit.builder()
                .juniperKitId(kitRequestId.toString())
                .currentStatus("SENT")
                .build();
    }

    @Override
    public Collection<PepperKit> fetchKitStatusByStudy(String studyShortcode) {
        log.info("STUB fetching status by study");
        var studyEnvironment = studyEnvironmentDao.findByStudy(studyShortcode, EnvironmentName.sandbox).get();
        return kitRequestService.findByStudyEnvironment(studyEnvironment.getId()).stream().map(kit -> {
            PepperKit pepperKit = PepperKit.builder()
                    .juniperKitId(kit.getId().toString())
                    .currentStatus(getNextStatus(kit))
                    .build();
            return addFieldsForStatus(pepperKit, kit);
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
            PepperKit pepperStatus = objectMapper.readValue(kit.getExternalKit(), PepperKit.class);
            String currentStatus = pepperStatus.getCurrentStatus();
            int currentStatusIndex = statusVals.indexOf(currentStatus);
            int nextStatusIndex = (currentStatusIndex + 1) % statusVals.size();
            return statusVals.get(nextStatusIndex);
        } catch (Exception e) {
            return "error";
        }
    }

    private PepperKit addFieldsForStatus(PepperKit pepperKit, KitRequest kitRequest) {
        PepperKitStatus status = PepperKitStatus.fromCurrentStatus(pepperKit.getCurrentStatus());
        if (!List.of(PepperKitStatus.SENT, PepperKitStatus.RECEIVED).contains(status)) {
            // remove leftover fields from previous status
            if (List.of(PepperKitStatus.CREATED, PepperKitStatus.QUEUED).contains(status)) {
                pepperKit.setLabelDate(null);
                pepperKit.setScanDate(null);
                pepperKit.setReceiveDate(null);
            }
            return pepperKit;
        }
        Instant createdTime = kitRequest.getCreatedAt();
        Instant labelTime = createdTime.plus(2, ChronoUnit.DAYS);
        Instant scanTime = labelTime.plus(5, ChronoUnit.MINUTES);

        pepperKit.setLabelDate(labelTime.toString());
        pepperKit.setScanDate(scanTime.toString());

        if (status == PepperKitStatus.RECEIVED) {
            Instant receivedTime = scanTime.plus(7, ChronoUnit.DAYS);
            pepperKit.setReceiveDate(receivedTime.toString());
        }
        return pepperKit;
    }
}
