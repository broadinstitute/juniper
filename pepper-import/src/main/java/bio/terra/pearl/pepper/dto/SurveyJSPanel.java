package bio.terra.pearl.pepper.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SurveyJSPanel {
    @Builder.Default
    String type = "panel";

    String name;
    Map<String,String> title;

    List<JsonNode> elements;

    List<JsonNode> templateElements;

    Map<String, String> templateTitle;
    Map<String, String> panelAddText;

    String visibleIf;
}
