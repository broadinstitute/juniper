package bio.terra.pearl.core.model.survey;

public enum SurveyType {
    PRE_ENROLL, // for prenrollment surveys
    CONSENT, // for consent forms
    RESEARCH, // for surveys intended to be included in the research dataset of a study
    OUTREACH, // for surveys intended for outreach purposes, e.g. to collect marketing/feedback information
    ADMIN, // for surveys intended for study staff purposes, e.g. for data entry
    DOCUMENT_REQUEST, // for surveys intended to request documents from participants
}
