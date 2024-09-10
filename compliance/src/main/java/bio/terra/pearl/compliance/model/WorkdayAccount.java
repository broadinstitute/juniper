package bio.terra.pearl.compliance.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Collection;

@Getter @Setter @SuperBuilder @NoArgsConstructor @ToString
public class WorkdayAccount extends VantaObject {

    // this is full name (first + last)
    private String displayName;

    @Override
    public boolean shouldBeInScope(Collection<PersonInScope> peopleInScope) {
        return peopleInScope.stream().anyMatch(personInScope -> displayName.equalsIgnoreCase(personInScope.getFullName()));
    }

    @Override
    public String getSimpleId() {
        return displayName;
    }

}
