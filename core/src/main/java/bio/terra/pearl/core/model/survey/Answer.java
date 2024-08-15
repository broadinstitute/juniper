package bio.terra.pearl.core.model.survey;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Objects;
import java.util.UUID;

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
    private String viewedLanguage;
    private AnswerType answerType;
    private String stringValue;
    // objects are stored as JSON strings
    private String objectValue;
    // store all numbers as doubles to match Javascript/JSON.
    private Double numberValue;
    private Boolean booleanValue;

    public void setCreatingEntity(ResponsibleEntity responsibleEntity) {
        this.creatingParticipantUserId = responsibleEntity.getParticipantUser() != null ? responsibleEntity.getParticipantUser().getId() : null;
        this.creatingAdminUserId = responsibleEntity.getAdminUser() != null ? responsibleEntity.getAdminUser().getId() : null;
    }

    public void setValueAndType(Object value) {
        answerType = AnswerType.forValue(value);
        setValue(value);
    }

    /** infers the type based on what value was set */
    public void inferTypeIfMissing() {
        if (answerType != null) {
            return;
        }
        if (stringValue != null) {
            answerType = AnswerType.STRING;
        } else if (numberValue != null) {
            answerType = AnswerType.NUMBER;
        } else if (booleanValue != null) {
            answerType = AnswerType.BOOLEAN;
        } else if (objectValue != null) {
            answerType = AnswerType.OBJECT;
        }
    }

    protected void setValue(Object value) {
        if (answerType.equals(AnswerType.STRING)) {
            stringValue = (String) value;
        } else if (answerType.equals(AnswerType.NUMBER)) {
            numberValue = ((Number) value).doubleValue();
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
        viewedLanguage = answer.viewedLanguage;
    }

    public boolean valuesEqual(Answer answer) {
        return Objects.equals(booleanValue, answer.booleanValue) &&
                Objects.equals(stringValue, answer.stringValue) &&
                Objects.equals(numberValue, answer.numberValue) &&
                Objects.equals(objectValue, answer.objectValue) &&
                Objects.equals(otherDescription, answer.otherDescription);
    }
}
