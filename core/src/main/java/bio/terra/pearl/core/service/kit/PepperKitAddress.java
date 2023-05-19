package bio.terra.pearl.core.service.kit;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Address fields included in the request to DSM when requesting a kit.
 */
@Getter @SuperBuilder
public class PepperKitAddress {
    String firstName;
    String lastName;
    String street1;
    String street2;
    String city;
    String state;
    String postalCode;
    String country;
    String phoneNumber;
}
