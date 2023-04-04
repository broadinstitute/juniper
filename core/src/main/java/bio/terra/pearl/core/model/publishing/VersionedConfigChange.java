package bio.terra.pearl.core.model.publishing;

import java.util.List;
import java.util.UUID;

public record VersionedConfigChange(UUID sourceId, UUID destId, List<ConfigChange> configChanges,
                                    VersionedEntityChange templateChange) {
    public boolean isChanged() {
        return !configChanges.isEmpty() || templateChange().isChanged();
    }

}
