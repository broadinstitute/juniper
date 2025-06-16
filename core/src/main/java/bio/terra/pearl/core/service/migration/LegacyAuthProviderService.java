package bio.terra.pearl.core.service.migration;

import bio.terra.pearl.core.model.migration.AuthMigrationConfig;

public interface LegacyAuthProviderService {
    boolean attemptLogIn(
            AuthMigrationConfig config,
            String email,
            String password
    );
}
