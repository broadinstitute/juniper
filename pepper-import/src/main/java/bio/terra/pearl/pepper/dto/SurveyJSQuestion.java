package bio.terra.pearl.pepper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SurveyJSQuestion {
    public String name;
    public String type;
    public String inputType;
    public Map<String, String> title;
    public boolean isRequired;
    public Map<String, String> requiredText;
    public List<Choice> choices;
    public String visibleIf;
    @Builder.Default
    public List<SurveyJsValidator> validators = new ArrayList<>();

    @AllArgsConstructor
    @Data
    public static class Choice {
        public Map<String, String> text;
        public String value;
    }

}
