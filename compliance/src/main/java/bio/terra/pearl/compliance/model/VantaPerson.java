package bio.terra.pearl.compliance.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Collection;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class VantaPerson extends VantaObject {

    // this id is used as the owner id for jamf computers
    private String id;

    private String emailAddress;

    @Override
    public boolean shouldBeInScope(Collection<PersonInScope> peopleInScope) {
        return peopleInScope.stream().anyMatch(personInScope -> emailAddress.equalsIgnoreCase(personInScope.getEmail()));
    }

    @Override
    public String getSimpleId() {
        return emailAddress;
    }
}
