package bio.terra.pearl.core.service.kit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.service.kit.pepper.PepperKit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class KitRequestDtoTest extends BaseSpringBootTest {

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void testUpdateExternalKit() {
    try {
      PepperKit pepperKit = PepperKit.builder()
          .juniperKitId(UUID.randomUUID().toString())
          .currentStatus("SENT")
          .dsmShippingLabel(UUID.randomUUID().toString())
          .labelByEmail("jane@test.com")
          .errorDate(
              DateTimeFormatter.ISO_LOCAL_DATE_TIME
                  .withZone(ZoneId.systemDefault())
                  .format(Instant.now()))
          .build();

      KitRequest kitRequest = KitRequest.builder()
          .id(UUID.randomUUID())
          .externalKit(objectMapper.writeValueAsString(pepperKit))
          .build();
      String details = KitRequestDto.createRequestDetails(kitRequest, objectMapper);

      JsonNode jsonDetails = objectMapper.readTree(details);
      assertThat(jsonDetails.get("requestId").textValue(), equalTo(kitRequest.getId().toString()));
      assertThat(
          jsonDetails.get("shippingId").textValue(), equalTo(pepperKit.getDsmShippingLabel()));
      assertThat(jsonDetails.get("errorDate").textValue(), equalTo(pepperKit.getErrorDate()));
    } catch (Exception e) {
      Assertions.fail("Failed to build KitRequestDto", e);
    }
  }
}
