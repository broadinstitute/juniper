package bio.terra.pearl.core.model.survey;

import bio.terra.pearl.core.model.BaseEntity;
import java.util.Objects;
import java.util.UUID;
import bio.terra.pearl.core.model.portal.PortalEnvironmentLanguage;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * an answer to a survey question
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Answer extends BaseEntity {
    // either the adminUserId or participantUserId represents the user who submitted this data.  Only one should
    // exist for a given answer
    private UUID creatingAdminUserId;
    private UUID creatingParticipantUserId;
    private UUID surveyResponseId;
    private UUID enrolleeId;
    private String questionStableId;
    private String surveyStableId;
    private String otherDescription;
    private int surveyVersion;
    private PortalEnvironmentLanguage viewedLanguage;
    private UUID viewedLanguageId;
    private AnswerType answerType;
    private String stringValue;
    // objects are stored as JSON strings
    private String objectValue;
    // store all numbers as doubles to match Javascript/JSON.
    private Double numberValue;
    private Boolean booleanValue;

    public void setValueAndType(Object value) {
        answerType = AnswerType.forValue(value);
        setValue(value);
    }

    protected void setValue(Object value) {
        if (answerType.equals(AnswerType.STRING)) {
            stringValue = (String) value;
        } else if (answerType.equals(AnswerType.NUMBER)) {
            numberValue = (Double) value;
        } else if (answerType.equals(AnswerType.BOOLEAN)) {
            booleanValue = (Boolean) value;
        } else {
            objectValue = (String) value;
        }
    }

    public String valueAsString() {
        if (objectValue != null) {
            return objectValue;
        } else if (booleanValue != null) {
            return booleanValue.toString();
        } else if (numberValue != null) {
            return numberValue.toString();
        }
        return stringValue;
    }

    // dumb copy of answer values and type
    public void copyValuesFrom(Answer answer) {
        stringValue = answer.stringValue;
        booleanValue = answer.booleanValue;
        numberValue = answer.numberValue;
        objectValue = answer.objectValue;
        answerType = answer.answerType;
        otherDescription = answer.otherDescription;
    }

    public boolean valuesEqual(Answer answer) {
        return Objects.equals(booleanValue, answer.booleanValue) &&
                Objects.equals(stringValue, answer.stringValue) &&
                Objects.equals(numberValue, answer.numberValue) &&
                Objects.equals(objectValue, answer.objectValue) &&
                Objects.equals(otherDescription, answer.otherDescription);
    }
}
