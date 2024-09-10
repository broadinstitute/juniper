package bio.terra.pearl.compliance.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor
public class UpdateVantaMetadata {

    private boolean inScope;

    private final List<String> resourceIds = new ArrayList<>();

    public void addResourceId(String resourceId) {
        this.resourceIds.add(resourceId);
    }
}
