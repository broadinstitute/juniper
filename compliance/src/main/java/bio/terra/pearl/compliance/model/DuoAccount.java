package bio.terra.pearl.compliance.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Collection;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@ToString
public class DuoAccount extends VantaObject {

    String accountName;

    @Override
    public boolean shouldBeInScope(Collection<PersonInScope> peopleInScope) {
        return peopleInScope.stream().anyMatch(personInScope -> (accountName + "@broadinstitute.org").equalsIgnoreCase(personInScope.getEmail()));
    }

    @Override
    public String getSimpleId() {
        return "";
    }
}
