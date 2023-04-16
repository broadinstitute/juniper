package bio.terra.pearl.core.model.survey;

import bio.terra.pearl.core.model.BaseEntity;
import java.util.UUID;
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
    private int surveyVersion;
    private AnswerType answerType;
    private String stringValue;
    // objects are stored as JSON strings
    private String objectValue;
    // store all numbers as doubles to match Javascript/JSON.
    private Double numberValue;

//    public Object getValue() {
//        if (Objects.equals(answerType, AnswerType.STRING)) {
//            return stringValue;
//        } else if (Objects.equals(answerType, AnswerType.NUMBER)) {
//            return numberValue;
//        } else {
//            return objectValue;
//        }
//    }
//
//    public void setValue(Object value) {
//        if (answerType == null) {
//            answerType = AnswerType.forValue(value);
//        }
//        if (answerType.equals(AnswerType.STRING)) {
//            stringValue = (String) value;
//        } else if (answerType.equals(AnswerType.NUMBER)) {
//            numberValue = (Double) value;
//        } else {
//            objectValue = (String) value;
//        }
//    }
}
