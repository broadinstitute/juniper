package bio.terra.pearl.core.model.survey;

public enum RecurrenceType {
    NONE,
    LONGITUDINAL, // when it recurs, each instance is a new response
    UPDATE  // when it recurs, update existing response (e.g 'what is your address?')
}
