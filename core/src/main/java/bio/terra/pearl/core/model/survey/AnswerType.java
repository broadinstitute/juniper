package bio.terra.pearl.core.model.survey;

public enum AnswerType {
    STRING,
    NUMBER,
    OBJECT;

    public static AnswerType forValue(Object value) {
        if (value instanceof String) {
            return STRING;
        } else if (value instanceof Double) {
            return NUMBER;
        }
        return OBJECT;
    }
}
