package bio.terra.pearl.core.model.participant;

public enum EnrolleeWithdrawalReason {
    TESTING, // was created for testing or admin purposes, and does not represent a real participant
    PARTICIPANT_REQUEST, // a true withdrawal by the participant
    DUPLICATE // a duplicate enrollee was inadvertently created (likely by the same participant signing up twice, this was withdrawing the duplicate
}
