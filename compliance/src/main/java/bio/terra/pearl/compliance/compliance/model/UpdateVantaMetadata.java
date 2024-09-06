package bio.terra.pearl.compliance.compliance.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @SuperBuilder @NoArgsConstructor
public class UpdateVantaMetadata {

    private boolean inScope;

    private List<String> resourceIds = new ArrayList<>();

    public void addResourceId(String resourceId) {
        this.resourceIds.add(resourceId);
    }
}
