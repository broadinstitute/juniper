package bio.terra.pearl.core.model.site;

public enum NavbarItemType {
    INTERNAL, // this is a relative link, e.g. /aboutUs
    EXTERNAL, // link to external site, e.g. https://nih.gov
    MAILING_LIST, // pop up an invite to join the mailing list for the study
    REGISTER // the registration link, a.k.a. "Join us"
}
