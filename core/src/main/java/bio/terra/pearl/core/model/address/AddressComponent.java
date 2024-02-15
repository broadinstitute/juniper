package bio.terra.pearl.core.model.address;

public enum AddressComponent {
    HOUSE_NUMBER,
    STREET_NAME,
    STREET_TYPE,
    CITY,
    STATE_PROVINCE,
    COUNTRY,
    POSTAL_CODE,
    SUBPREMISE; // subpremise - need if, e.g., an apartment building
}
