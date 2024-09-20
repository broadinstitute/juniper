package bio.terra.pearl.compliance.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

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
