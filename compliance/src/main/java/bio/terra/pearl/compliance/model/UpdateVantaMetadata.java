package bio.terra.pearl.compliance.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor
public class UpdateVantaMetadata {

    private final List<SingleUpdate> updates = new ArrayList<>();

    public void add(String resourceId, boolean inScope) {
        updates.add(new SingleUpdate(resourceId, inScope));
    }

    public int size() {
        return updates.size();
    }
}
