package bio.terra.pearl.compliance.model;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class SingleUpdate {

    private String id;

    private boolean inScope;

    public SingleUpdate(String resourceId, boolean inScope) {
        this.id = resourceId;
        this.inScope = inScope;
    }
}
