package bio.terra.pearl.compliance.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Collection;

@Getter @Setter @SuperBuilder @NoArgsConstructor @ToString
public class SlackUser extends VantaObject {

    private String accountName;

    @Override
    public String getIntegrationId() {
        return "slack";
    }

    @Override
    public boolean shouldBeInScope(Collection<PersonInScope> peopleInScope) {
        return peopleInScope.stream().anyMatch(personInScope -> accountName.equalsIgnoreCase(personInScope.getEmail()));
    }

    @Override
    public String getSimpleId() {
        return accountName;
    }

}
