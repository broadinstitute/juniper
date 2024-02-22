package bio.terra.pearl.core.service.participant.search.facets;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Much of this class mimics some of the type processing found in Answer.java.  However, we don't support querying
 * objectValue stuff yet.
 */
@Getter
@Setter
@NoArgsConstructor
public class AnswerFacetValue implements FacetValue {
  private String stringValue;
  private Boolean booleanValue;
  private Double numberValue;
  private String questionStableId;
  private String surveyStableId;

  public AnswerFacetValue(String surveyStableId, String questionStableId, String value) {
        this.stringValue = value;
        this.questionStableId = questionStableId;
      this.surveyStableId = surveyStableId;
  }

    public AnswerFacetValue(String surveyStableId, String questionStableId, Boolean value) {
        this.booleanValue = value;
        this.questionStableId = questionStableId;
        this.surveyStableId = surveyStableId;
    }

    public AnswerFacetValue(String surveyStableId, String questionStableId, Double value) {
        this.numberValue = value;
        this.questionStableId = questionStableId;
        this.surveyStableId = surveyStableId;
    }

    public String valueAsString() {
        if (booleanValue != null) {
            return booleanValue.toString();
        } else if (numberValue != null) {
            return numberValue.toString();
        }
        return stringValue;
    }

    public Object getValue() {
        if (booleanValue != null) {
            return booleanValue;
        } else if (numberValue != null) {
            return numberValue;
        }
        return stringValue;
    }

    @Override
    public String getKeyName() {
        return "%s_%s_%s".formatted(surveyStableId, questionStableId, valueAsString());
    }

    @Override
    public void setKeyName(String keyName) {

    }
}
