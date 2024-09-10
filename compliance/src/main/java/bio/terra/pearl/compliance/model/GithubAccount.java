package bio.terra.pearl.compliance.model;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Collection;

@Getter @Setter @SuperBuilder @NoArgsConstructor @ToString
public class GithubAccount extends VantaObject {

    private String displayName;

    private String owner;

    private String description;

    private String accountName;

    @Override
    public boolean shouldBeInScope(Collection<PersonInScope> peopleInScope) {
        return peopleInScope.stream().anyMatch(personInScope -> accountName.equalsIgnoreCase(personInScope.getGitUser()));
    }

    @Override
    public String getSimpleId() {
        return getDisplayName();
    }
}
