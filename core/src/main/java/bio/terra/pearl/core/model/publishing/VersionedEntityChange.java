package bio.terra.pearl.core.model.publishing;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.Versioned;

import java.util.Objects;
import java.util.UUID;

/** use Integers since they might be null if the oldValue was uninitialized */
public record VersionedEntityChange<T extends BaseEntity & Versioned>(String oldStableId, Integer oldVersion,
                                                                      String newStableId, Integer newVersion,
                                                                      UUID portalId) {
    public boolean isChanged() {
        return !Objects.equals(oldStableId, newStableId) || !Objects.equals(oldVersion, newVersion);
    }
    public VersionedEntityChange(Versioned source, Versioned dest) {
        this(dest != null ? dest.getStableId() : null,
                dest != null ? dest.getVersion() : null,
                source != null ? source.getStableId() : null,
                source != null ? source.getVersion() : null,
                source != null ? source.getPortalId() : dest != null ? dest.getPortalId() : null);
    }
}
