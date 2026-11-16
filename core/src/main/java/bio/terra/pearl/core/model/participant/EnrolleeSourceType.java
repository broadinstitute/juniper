package bio.terra.pearl.core.model.participant;

public enum EnrolleeSourceType {
    IMPORT, // the enrollee data was imported from an external source
    PORTAL_SITE, // default, they came in through standard signup flow
    MANUAL_ADMIN // created manually by study staff (e.g. adding a family member)
}
