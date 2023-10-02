package bio.terra.pearl.core.service.kit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Getter @Setter
@SuperBuilder @NoArgsConstructor
@EqualsAndHashCode
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PepperResponse {
    @NotNull
    @JsonProperty("isError")
    private Boolean isError;

    public static boolean checkIsError(String json, ObjectMapper objectMapper) throws JsonProcessingException {
        var parsedResponse = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        var isError = (Boolean) parsedResponse.get("isError");
        return isError != null ? isError : false;
    }
}
