package bio.terra.pearl.core.service.participant.search.facets;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jdbi.v3.core.statement.Query;


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
        // no-op
    }

    @Override
    public String getWhereClause(String tableName, String columnName, int facetIndex) {
        return """
                 answer.%s = :%s
                 and answer.survey_stable_id = :%s
                 and answer.question_stable_id = :%s    
                """.formatted(
                columnName,
                getValueParam(facetIndex),
                getSurveyStableIdParam(facetIndex),
                getQuestionStableIdParam(facetIndex)
        );
    }

    @Override
    public void bindSqlParameters(String tableName, String columnName, int facetIndex, Query query) {
        query.bind(getValueParam(facetIndex), getValue());
        query.bind(getSurveyStableIdParam(facetIndex), getSurveyStableId());
        query.bind(getQuestionStableIdParam(facetIndex), getQuestionStableId());
    }

    private String getValueParam(int facetIndex) {
        return "answer_val_%s".formatted(facetIndex);
    }
    private String getQuestionStableIdParam(int facetIndex) {
        return "question_stable_id_%s".formatted(facetIndex);
    }

    private String getSurveyStableIdParam(int facetIndex) {
        return "survey_stable_id_%s".formatted(facetIndex);
    }
}
