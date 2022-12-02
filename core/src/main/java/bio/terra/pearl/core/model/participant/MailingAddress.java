package bio.terra.pearl.core.model.participant;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter @SuperBuilder @NoArgsConstructor
public class MailingAddress extends BaseEntity {
    private String street1;

    private String street2;

    private String state;

    private String country;

    private String city;

    private String zip;
}
