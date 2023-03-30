package bio.terra.pearl.core.model.publishing;

import bio.terra.pearl.core.model.Versioned;
import java.util.Objects;

/** use Integers since they might be null if the oldValue was uninitialized */
public record VersionedEntityChangeRecord(String oldStableId, Integer oldVersion,
                                          String newStableId, Integer newVersion) {
    public boolean isChanged() {
        return !Objects.equals(oldStableId, newStableId) || !Objects.equals(oldVersion, newVersion);
    }
    public VersionedEntityChangeRecord(Versioned source, Versioned dest) {
        this(dest != null ? dest.getStableId() : null,
                dest != null ? dest.getVersion() : null,
                source != null ? source.getStableId() : null,
                source != null ? source.getVersion() : null);
    }
}
