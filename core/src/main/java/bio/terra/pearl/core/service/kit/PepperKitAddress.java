package bio.terra.pearl.core.service.kit;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Address fields included in the request to DSM when requesting a kit.
 */
@Getter @SuperBuilder
public class PepperKitAddress {
    private String firstName;
    private String lastName;
    private String street1;
    private String street2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String phoneNumber;
}
