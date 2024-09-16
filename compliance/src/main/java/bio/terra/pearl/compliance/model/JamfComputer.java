package bio.terra.pearl.compliance.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Collection;

@Getter @Setter @SuperBuilder @NoArgsConstructor @ToString
public class JamfComputer extends VantaObject {

    // this is the /v1/people/ -> data.id of the owner of the computer
    private String owner;

    private String displayName;

    @Override
    public boolean shouldBeInScope(Collection<PersonInScope> peopleInScope) {
        return peopleInScope.stream().anyMatch(personInScope -> {
            if (personInScope.getVantaPerson() != null) {
                return owner!= null && owner.equalsIgnoreCase(personInScope.getVantaPerson().getId());
            } else {
                return false;
            }
        });
    }

    @Override
    public String getSimpleId() {
        return owner + " " + displayName;
    }
}
