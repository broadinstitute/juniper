package bio.terra.pearl.compliance.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter @SuperBuilder @NoArgsConstructor
public class PersonInScope {

    private String email;

    private String gitUser;

    private String firstName;

    private String lastName;

    private VantaPerson vantaPerson;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
