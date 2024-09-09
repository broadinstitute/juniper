package bio.terra.pearl.compliance.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Collection;

@Getter @Setter @SuperBuilder @NoArgsConstructor
public class WorkdayAccount extends VantaObject {

    // this is full name (first + last)
    private String displayName;

    @Override
    public String getIntegrationId() {
        return "workday";
    }

    @Override
    public boolean shouldBeInScope(Collection<PersonInScope> peopleInScope) {
        return peopleInScope.stream().anyMatch(personInScope -> displayName.equalsIgnoreCase(personInScope.getFullName()));
    }

    @Override
    public String getSimpleId() {
        return displayName;
    }

    @Override
    public String toString() {
        return "WorkdayAccount{" +
                "displayName='" + displayName + '\'' +
                ", responseType='" + responseType + '\'' +
                ", resourceKind='" + resourceKind + '\'' +
                ", resourceId='" + resourceId + '\'' +
                ", connectionId='" + connectionId + '\'' +
                ", isDeactivated=" + isDeactivated +
                ", inScope=" + inScope +
                '}';
    }
}
