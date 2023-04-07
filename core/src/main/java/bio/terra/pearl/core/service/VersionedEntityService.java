package bio.terra.pearl.core.service;

import java.util.Optional;

public interface VersionedEntityService<T> {
    Optional<T> findByStableId(String stableId, int version);
}
