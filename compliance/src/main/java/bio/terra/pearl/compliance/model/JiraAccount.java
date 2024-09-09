package bio.terra.pearl.compliance.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Collection;

@Getter @Setter @SuperBuilder @NoArgsConstructor
public class JiraAccount extends VantaObject {

    // this is full name (first + last)
    private String accountName;

    @Override
    public String getIntegrationId() {
        return "jira";
    }

    @Override
    public boolean shouldBeInScope(Collection<PersonInScope> peopleInScope) {
        return peopleInScope.stream().anyMatch(personInScope -> accountName.equalsIgnoreCase(personInScope.getFullName()));
    }

    @Override
    public String getSimpleId() {
        return accountName;
    }

    @Override
    public String toString() {
        return "JiraAccount{" +
                "accountName ='" + accountName + '\'' +
                ", responseType='" + responseType + '\'' +
                ", resourceKind='" + resourceKind + '\'' +
                ", resourceId='" + resourceId + '\'' +
                ", connectionId='" + connectionId + '\'' +
                ", isDeactivated=" + isDeactivated +
                ", inScope=" + inScope +
                '}';
    }
}
