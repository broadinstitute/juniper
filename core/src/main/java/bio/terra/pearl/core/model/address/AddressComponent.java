package bio.terra.pearl.core.model.address;

public enum AddressComponent {
    HOUSE_NUMBER("house_number"),
    STREET_NAME("street_name"),
    STREET_TYPE("street_type"),
    CITY("city"),
    STATE_PROVINCE("state_province"),
    COUNTRY("country"),
    POSTAL_CODE("postal_code"),
    SUBPREMISE("subpremise"); // subpremise - need if, e.g., an apartment building
    private final String value;

    AddressComponent(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
