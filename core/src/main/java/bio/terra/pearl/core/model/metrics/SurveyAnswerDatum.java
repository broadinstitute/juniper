package bio.terra.pearl.core.model.metrics;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class SurveyAnswerDatum {
    private String name;
    private String stringValue;
    private String objectValue;
    private Double numberValue;
    private Boolean booleanValue;
    private Instant time;
}
