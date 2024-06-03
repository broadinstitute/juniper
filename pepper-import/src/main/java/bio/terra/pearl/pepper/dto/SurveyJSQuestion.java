package bio.terra.pearl.pepper.dto;

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
public class SurveyJSQuestion {
    public String name;
    public String type;
    public String inputType;
    public Map<String, String> title;
    public boolean required;
    //public boolean isRequired;
    public List<Choice> choices;

    @AllArgsConstructor
    @Data
    public static class Choice {
        public Map<String, String> text;
        public String value;
    }

}
