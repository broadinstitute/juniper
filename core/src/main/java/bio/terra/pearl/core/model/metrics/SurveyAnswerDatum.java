package bio.terra.pearl.core.model.metrics;

import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.AnswerType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Builder @Getter @Setter
public class SurveyAnswerDatum {
    private String stringValue;
    private String objectValue;
    private Double numberValue;
    private Boolean booleanValue;
    private Instant time;
}
