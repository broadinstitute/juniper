package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.model.kit.DistributionMethod;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** the admin controller builds the creation dto from the raw request body with convertValue */
class KitRequestCreationDtoTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void attestationDefaultsToFalseWhenAbsent() {
        var dto = objectMapper.convertValue(
                Map.of("kitType", "SALIVA", "distributionMethod", "MAILED", "skipAddressValidation", false),
                KitRequestService.KitRequestCreationDto.class);
        assertEquals("SALIVA", dto.kitType());
        assertEquals(DistributionMethod.MAILED, dto.distributionMethod());
        assertFalse(dto.attestationAccepted());
    }

    @Test
    void readsAttestation() {
        var dto = objectMapper.convertValue(
                Map.of("kitType", "SALIVA", "distributionMethod", "MAILED", "attestationAccepted", true),
                KitRequestService.KitRequestCreationDto.class);
        assertTrue(dto.attestationAccepted());
    }
}
