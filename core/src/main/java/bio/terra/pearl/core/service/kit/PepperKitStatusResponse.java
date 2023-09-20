package bio.terra.pearl.core.service.kit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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

    /**
     * Minimally parses the given JSON as a PepperKitStatusResponse to extract the list of kits.
     */
    public static List<Object> extractUntypedKitStatuses(String json, ObjectMapper objectMapper) throws JsonProcessingException {
        var parsedResponse = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        // This cast won't fail as long as kits is an array
        return (List<Object>) parsedResponse.get("kits");
    }
}
