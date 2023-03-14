package bio.terra.pearl.core.model.participant;

import bio.terra.pearl.core.model.BaseEntity;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter @Setter @SuperBuilder @NoArgsConstructor
public class Profile extends BaseEntity {
    private String givenName;

    private String familyName;

    private MailingAddress mailingAddress;
    private UUID mailingAddressId;

    private String contactEmail;
    private boolean doNotEmail; // do not send any emails to this user
    private boolean doNotEmailSolicit; // do not send any emails not directly related to study participation

    private LocalDate birthDate;
    private String phoneNumber;

    public static abstract class ProfileBuilder<C extends Profile, B extends Profile.ProfileBuilder<C, B>>
            extends BaseEntity.BaseEntityBuilder<C, B> {
        @Getter
        private MailingAddress mailingAddress;
        @Getter
        private UUID mailingAddressId;

        public Profile.ProfileBuilder mailingAddress(MailingAddress mailingAddress) {
            this.mailingAddress = mailingAddress;
            this.mailingAddressId = mailingAddress.getId();
            return this;
        }
    }
}
