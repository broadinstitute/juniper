package bio.terra.pearl.core.service.kit;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class DataStudyManagerKitRequest {
    private String firstName;
    private String lastName;
    private String street1;
    private String street2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String phoneNumber;
    private String juniperKitId;
    private String juniperParticipantId;
    private String juniperStudyId;
    private boolean skipAddressValidation;
    private boolean uploadAnyway;

    public static DataStudyManagerKitRequestBuilder builderWithAddress(PepperKitAddress address) {
        return DataStudyManagerKitRequest.builder()
                .firstName(address.getFirstName())
                .lastName(address.getLastName())
                .street1(address.getStreet1())
                .street2(address.getStreet2())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .phoneNumber(address.getPhoneNumber());
    }
}
