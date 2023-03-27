package bio.terra.pearl.core.model.publishing;

import bio.terra.pearl.core.model.Versioned;

/** use Integers since they might be null if the oldValue was uninitialized */
public record VersionedEntityChangeRecord(String oldStableId, Integer oldVersion,
                                          String newStableId, Integer newVersion) {

    public VersionedEntityChangeRecord(Versioned source, Versioned dest) {
        this(dest != null ? dest.getStableId() : null,
                dest != null ? dest.getVersion() : null,
                source != null ? source.getStableId() : null,
                source != null ? source.getVersion() : null);
    }
}
