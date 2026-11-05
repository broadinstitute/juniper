package bio.terra.pearl.core.service.kit.sidr;

import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.service.kit.sidr.models.SIDRLinks;
import bio.terra.pearl.core.service.kit.sidr.models.SIDROrder;
import bio.terra.pearl.core.service.kit.sidr.models.SIDRPatient;
import bio.terra.pearl.core.service.kit.sidr.models.SIDRSample;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Stand-in for SIDR that never makes a network call. Orders "succeed" with generated ids, except that, mirroring
 * the stub Pepper client, an address whose street or city starts with "BAD" is rejected as invalid (unless address
 * verification is overridden).
 */
@Component
@Slf4j
public class StubSIDRClient implements SIDRClient {
    public static final String BAD_ADDRESS_PREFIX = "BAD";
    public static final String STUB_ORDER_PREFIX = "SDOR-STUB-";

    @Override
    public SIDROrder postOrder(StudyEnvironmentConfig config, AdminUser actor, SIDROrder order) throws SIDRApiException {
        log.info("STUB placing SIDR order");
        for (SIDRSample sample : order.getSamples() == null ? List.<SIDRSample>of() : order.getSamples()) {
            if (hasBadAddress(sample)) {
                throw SIDRApiException.fromResponse(HttpStatus.UNPROCESSABLE_ENTITY,
                        "{\"errors\":[{\"type\":\"" + SIDRApiException.ADDRESS_INVALID_TYPE
                                + "\",\"msg\":\"STUB: Address is invalid\"}]}");
            }
        }
        String orderId = STUB_ORDER_PREFIX + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return SIDROrder.builder()
                .orderId(orderId)
                .projectId(order.getProjectKey())
                .projectKey(order.getProjectKey())
                .externalOrderId(order.getExternalOrderId())
                .numberOfSamples(order.getNumberOfSamples())
                .sampleKitRequested(order.getSampleKitRequested())
                .tests(order.getTests())
                .samples(order.getSamples() == null ? List.of() : order.getSamples().stream()
                        .map(sample -> stubSample(sample, orderId)).toList())
                .links(SIDRLinks.builder().order("http://stub-sidr/api/order/" + orderId).build())
                .build();
    }

    @Override
    public SIDROrder getOrder(StudyEnvironmentConfig config, String sidrOrderId) throws SIDRApiException {
        log.info("STUB fetching SIDR order");
        return SIDROrder.builder()
                .orderId(sidrOrderId)
                .links(SIDRLinks.builder().order("http://stub-sidr/api/order/" + sidrOrderId).build())
                .build();
    }

    private static boolean hasBadAddress(SIDRSample sample) {
        if (sample.getShippingInformation() == null || sample.getShippingInformation().getPatientAddress() == null) {
            return false;
        }
        var address = sample.getShippingInformation().getPatientAddress();
        if (Boolean.FALSE.equals(address.getVerifyStrict())) {
            // like SIDR, accept whatever address we're given when strict verification is turned off
            return false;
        }
        return startsWithBad(address.getLine1()) || startsWithBad(address.getCity());
    }

    private static boolean startsWithBad(String value) {
        return value != null && value.startsWith(BAD_ADDRESS_PREFIX);
    }

    private static SIDRSample stubSample(SIDRSample requested, String orderId) {
        return SIDRSample.builder()
                .sidrSampleId("SDSM-STUB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .sampleId(requested.getSampleId())
                .materialType(requested.getMaterialType())
                .shippingInformation(requested.getShippingInformation())
                .patient(SIDRPatient.builder()
                        .patientId(requested.getPatient() == null ? null : requested.getPatient().getPatientId())
                        .sidrPatientId("SDPT-STUB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                        .build())
                .orderIds(List.of(orderId))
                .lastModified(Instant.now().toString())
                .build();
    }
}
