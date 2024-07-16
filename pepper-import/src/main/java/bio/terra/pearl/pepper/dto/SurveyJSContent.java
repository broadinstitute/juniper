package bio.terra.pearl.pepper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SurveyJSContent {
    public String type;
    public String name;
    public Map<String, String> html;
    public Map<String, String> title;
    public String visibleIf;

}
