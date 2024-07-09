package bio.terra.pearl.pepper.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Builder
public class SurveyJsValidator {
    String type;
    @Builder.Default
    Map<String, String> text = new HashMap<>();
    String regex;
    Long minValue;
    Long maxValue;
}
