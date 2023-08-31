package bio.terra.pearl.core.model.publishing;

import bio.terra.pearl.core.model.Versioned;
import java.util.UUID;

/**
 * A configuration that includes a versioned entity.
 * For example a config that has a Survey and also properties
 * for how that survey should be taken.
 */
public interface VersionedEntityConfig {
    Versioned versionedEntity();
    UUID versionedEntityId();
    UUID getId();
    /** this is called update rather than set so it doesn't register as a bean property, and so doesn't get needlessly
     * duplicated for dao and serialization methods */
    void updateVersionedEntityId(UUID versionedEntityId);
}
