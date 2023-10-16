package bio.terra.pearl.core.service.kit.pepper;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Address fields included in the request to DSM when requesting a kit.
 */
@Getter
@SuperBuilder @NoArgsConstructor
@EqualsAndHashCode
@ToString
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
