package bio.terra.pearl.core.service.kit;

/**
 * Address fields included in the request to DSM when requesting a kit.
 */
public record PepperKitAddress(
        String firstName,
        String lastName,
        String street1,
        String street2,
        String city,
        String state,
        String postalCode,
        String country,
        String phoneNumber
) {}
