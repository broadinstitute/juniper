package bio.terra.pearl.core.model.publishing;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.Versioned;

import java.util.List;
import java.util.UUID;

public record VersionedConfigChange<T extends BaseEntity & Versioned>(UUID sourceId, UUID destId, List<ConfigChange> configChanges,
                                                                      VersionedEntityChange<T> documentChange) {
    public boolean isChanged() {
        return !configChanges.isEmpty() || documentChange().isChanged();
    }

}
