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
    public Map<String, String> placeholder;
    public Map<String, String> title;
    public String titleLocation;
    public Map<String, String> labelTrue;
    public Map<String, String> labelFalse;
    public String valueTrue;
    public String valueFalse;
    public String min;
    public Map<String, String> minErrorText;
    public String max;
    public Map<String, String> maxErrorText;
    public String minValueExpression;
    public String maxValueExpression;
    public Boolean isRequired;
    public Map<String, String> requiredErrorText;
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
