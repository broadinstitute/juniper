package bio.terra.pearl.core.model.publishing;

import bio.terra.pearl.core.model.Versioned;
import java.util.UUID;

/**
 * A configuration that includes a versioned entity.
 * For example a config that has a Survey and also properties
 * for how that survey should be taken.
 */
public interface VersionedEntityConfig {
    Versioned getVersionedEntity();
    UUID getId();
}
