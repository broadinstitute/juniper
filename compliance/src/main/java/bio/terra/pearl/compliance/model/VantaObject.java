package bio.terra.pearl.compliance.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Collection;

@Getter @Setter @SuperBuilder @NoArgsConstructor
public abstract class VantaObject {

    String responseType;

    String resourceKind;

    String resourceId;

    String connectionId;

    boolean isDeactivated;

    boolean inScope;

    public abstract boolean shouldBeInScope(Collection<PersonInScope> peopleInScope);

    public abstract String getSimpleId();
}
