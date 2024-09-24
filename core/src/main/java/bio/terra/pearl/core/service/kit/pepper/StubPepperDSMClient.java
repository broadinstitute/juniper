package bio.terra.pearl.core.service.kit.pepper;

import bio.terra.pearl.core.dao.study.StudyEnvironmentDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.kit.DistributionMethod;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.service.kit.KitRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
@Slf4j
public class StubPepperDSMClient implements PepperDSMClient {
    private final KitRequestService kitRequestService;
    private final StudyEnvironmentDao studyEnvironmentDao;
    private final ObjectMapper objectMapper;

    private static final String BAD_ADDRESS_PREFIX = "BAD";
    private static final String PEPPER_ADDRESS_VALIDATION_MSG = "ADDRESS_VALIDATION_ERROR";

    public StubPepperDSMClient(@Lazy KitRequestService kitRequestService,
                               StudyEnvironmentDao studyEnvironmentDao,
                               ObjectMapper objectMapper) {
        this.kitRequestService = kitRequestService;
        this.studyEnvironmentDao = studyEnvironmentDao;
        this.objectMapper = objectMapper;
    }

    @Override
    public PepperKit sendKitRequest(String studyShortcode, StudyEnvironmentConfig config, Enrollee enrollee, KitRequest kitRequest, PepperKitAddress address) {
        log.info("STUB sending kit request");
        if (kitRequest.getDistributionMethod().equals(DistributionMethod.MAILED) && (address.getCity().startsWith(BAD_ADDRESS_PREFIX) || address.getStreet1().startsWith(BAD_ADDRESS_PREFIX))) {
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
    public PepperKit fetchKitStatus(StudyEnvironmentConfig studyEnvironmentConfig, UUID kitRequestId) {
        log.info("STUB fetching kit status");
        return PepperKit.builder()
                .juniperKitId(kitRequestId.toString())
                .currentStatus("SENT")
                .build();
    }

    @Override
    public Collection<PepperKit> fetchKitStatusByStudy(String studyShortcode, StudyEnvironmentConfig config) {
        log.info("STUB fetching status by study");
        StudyEnvironment studyEnvironment = studyEnvironmentDao.findByStudy(studyShortcode, EnvironmentName.sandbox).get();
        return kitRequestService.findByStudyEnvironment(studyEnvironment.getId()).stream().map(kit -> {
            PepperKit pepperKit = PepperKit.builder()
                .juniperKitId(kit.getId().toString())
                .currentStatus(getNextStatus(kit))
                .dsmShippingLabel(createLabel())
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

    /** add and remove appropriate fields for the given status (i.e. sent and received dates) */
    private PepperKit addFieldsForStatus(PepperKit pepperKit, KitRequest kitRequest) {
        PepperKitStatus status = PepperKitStatus.fromCurrentStatus(pepperKit.getCurrentStatus());
        if (!List.of(PepperKitStatus.QUEUED, PepperKitStatus.SENT, PepperKitStatus.RECEIVED).contains(status)) {
            // remove leftover fields from previous status
            if (PepperKitStatus.CREATED.equals(status)) {
                pepperKit.setLabelDate(null);
                pepperKit.setScanDate(null);
                pepperKit.setReceiveDate(null);
            }
            if (PepperKitStatus.ERRORED.equals(status)) {
                pepperKit.setErrorMessage("Error processing request");
                pepperKit.setErrorDate(Instant.now().toString());
            } else {
                pepperKit.setErrorMessage(null);
                pepperKit.setErrorDate(null);           }
            return pepperKit;
        }

        // status is QUEUED, SENT or RECEIVED
        Instant createdTime = kitRequest.getCreatedAt();
        // uncomment these to test with more realistic dates
        // Instant labelTime = createdTime.plus(2, ChronoUnit.DAYS);
        // Instant scanTime = labelTime.plus(5, ChronoUnit.MINUTES);
        // Instant receivedTime = scanTime.plus(7, ChronoUnit.DAYS);

        if (status == PepperKitStatus.QUEUED) {
            Instant labelTime = Instant.now();
            assureLabelInfo(pepperKit, labelTime);
        } else if (status == PepperKitStatus.SENT) {
            // in case we skipped QUEUED
            assureLabelInfo(pepperKit, createdTime);

            Instant scanTime = Instant.now();
            assureScanInfo(pepperKit, scanTime);
        } else if (status == PepperKitStatus.RECEIVED) {
            // in case we skipped QUEUED
            assureLabelInfo(pepperKit, createdTime);
            // in case we skipped SENT
            assureScanInfo(pepperKit, createdTime);

            Instant receivedTime = Instant.now();
            pepperKit.setReceiveDate(receivedTime.toString());
            String returnTracking = pepperKit.getReturnTrackingNumber();
            if (returnTracking == null || returnTracking.isBlank()) {
                pepperKit.setReturnTrackingNumber(createLabel());
            }
        }
        return pepperKit;
    }

    private void assureLabelInfo(PepperKit pepperKit, Instant labelTime) {
        if (pepperKit.getLabelDate() == null) {
            pepperKit.setLabelDate(labelTime.toString());
        }
    }

    private void assureScanInfo(PepperKit pepperKit, Instant requestCreateTime) {
        if (pepperKit.getScanDate() == null) {
            pepperKit.setScanDate(requestCreateTime.toString());
        }
        String trackingNumber = pepperKit.getTrackingNumber();
        if (trackingNumber == null || trackingNumber.isBlank()) {
            pepperKit.setTrackingNumber(createLabel());
        }
    }

    private String createLabel() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
