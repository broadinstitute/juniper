package bio.terra.pearl.core.service.kit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class PepperDSMKitRequest {
    private JuniperKitRequest juniperKitRequest;
    @JsonProperty("juniperStudyGUID")
    private String juniperStudyId;
    private String kitType;
    private boolean uploadAnyway;

    @Getter @Builder
    public static class JuniperKitRequest {
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
        @JsonProperty("juniperParticipantID")
        private String juniperParticipantId;
        private boolean skipAddressValidation;

        public static JuniperKitRequestBuilder builderWithAddress(PepperKitAddress address) {
            return JuniperKitRequest.builder()
                    .firstName(address.getFirstName())
                    .lastName(address.getLastName())
                    .street1(address.getStreet1())
                    .street2(address.getStreet2())
                    .city(address.getCity())
                    .state(address.getState())
                    .postalCode(address.getPostalCode())
                    .country(address.getCountry())
                    .phoneNumber(address.getPhoneNumber());
        }
    }
}
