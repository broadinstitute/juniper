package bio.terra.pearl.core.model.survey;

public enum AnswerType {
    STRING,
    NUMBER,
    BOOLEAN,
    OBJECT;

    public static AnswerType forValue(Object value) {
        if (String.class.isInstance(value)) {
            return STRING;
        } else if (Double.class.isInstance(value)) {
            return NUMBER;
        } else if (Boolean.class.isInstance(value)) {
            return BOOLEAN;
        }
        return OBJECT;
    }
}
