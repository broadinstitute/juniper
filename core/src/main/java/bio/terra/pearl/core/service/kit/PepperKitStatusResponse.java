package bio.terra.pearl.core.service.kit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
public class PepperKitStatusResponse extends PepperResponse {
    private PepperKitStatus[] kits;

    public static List<Object> extractUntypedKitStatuses(JsonNode jsonNode, ObjectMapper objectMapper) {
        var valueType = objectMapper.constructType(new TypeReference<Map<String, Object>>() {});
        try {
            Map<String, Object> parsedResponse = objectMapper.treeToValue(jsonNode, valueType);
            return (List<Object>) parsedResponse.get("kits");
        } catch (JsonProcessingException e) {
            // TOOD: more detail in exception message
            throw new PepperException("Error parsing JSON from Pepper", e);
        }
    }
}
