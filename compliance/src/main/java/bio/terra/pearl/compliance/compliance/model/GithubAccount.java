package bio.terra.pearl.compliance.compliance.model;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Collection;

@Getter @Setter @SuperBuilder @NoArgsConstructor
public class GithubAccount extends VantaObject {

    @Override
    public String getIntegrationId() {
        return "github";
    }

    private String displayName, owner, description, accountName;

    private boolean isDeactivated, inScope;

    @Override
    public boolean shouldBeInScope(Collection<PersonInScope> peopleInScope) {
        return peopleInScope.stream().anyMatch(personInScope -> accountName.equalsIgnoreCase(personInScope.getGitUser()));
    }

    @Override
    public String getSimpleId() {
        return getDisplayName();
    }

    @Override
    public String toString() {
        return "GithubAccount{" +
                "displayName='" + displayName + '\'' +
                ", owner='" + owner + '\'' +
                ", description='" + description + '\'' +
                ", accountName='" + accountName + '\'' +
                ", isDeactivated=" + isDeactivated +
                ", inScope=" + inScope +
                ", responseType='" + responseType + '\'' +
                ", resourceKind='" + resourceKind + '\'' +
                ", resourceId='" + resourceId + '\'' +
                ", connectionId='" + connectionId + '\'' +
                ", isDeactivated=" + isDeactivated +
                ", inScope=" + inScope +
                '}';
    }
}
