package bio.terra.pearl.api.participant.config;

import static org.junit.jupiter.api.Assertions.*;

import bio.terra.pearl.api.participant.BaseSpringBootTest;
import bio.terra.pearl.core.model.participant.Enrollee;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class MappingConfigurationTest extends BaseSpringBootTest {
  @Autowired ObjectMapper objectMapper;

  @Test
  public void testObjectMapperDoesNotRenderResearchId() throws JsonProcessingException {
    Enrollee enrollee = Enrollee.builder().shortcode("HDSALK").researchId("12SALK").build();

    String json = objectMapper.writeValueAsString(enrollee);

    assertFalse(json.contains("researchId"));

    JsonNode parsed = objectMapper.readTree(json);

    assertTrue(parsed.isObject());
    assertFalse(parsed.has("researchId"));
    assertTrue(parsed.has("shortcode"));
    assertEquals("HDSALK", parsed.get("shortcode").textValue());
  }
}
