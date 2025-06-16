package bio.terra.pearl.core.service.migration;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.migration.AuthMigrationConfig;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@Slf4j
public class AuthMigrationService {
    private final StudyEnvironmentService studyEnvironmentService;
    private final AuthMigrationConfigService authMigrationConfigService;
    private final LegacyAuth0ProviderService auth0ProviderService;

    public AuthMigrationService(LegacyAuth0ProviderService auth0ProviderService, StudyEnvironmentService studyEnvironmentService, AuthMigrationConfigService authMigrationConfigService) {
        this.auth0ProviderService = auth0ProviderService;
        this.studyEnvironmentService = studyEnvironmentService;
        this.authMigrationConfigService = authMigrationConfigService;
    }

    public boolean attemptLegacyLogIn(
            String portalShortcode,
            String studyShortcode,
            EnvironmentName envName,
            String email,
            String password
    ) {

        StudyEnvironment studyEnvironment = studyEnvironmentService.findOne(
                portalShortcode,
                studyShortcode,
                envName
        ).orElseThrow(() -> new IllegalArgumentException(
                "Study environment not found for portal: " + portalShortcode +
                        ", study: " + studyShortcode +
                        ", environment: " + envName
        ));

        Optional<AuthMigrationConfig> authMigrationConfigOpt = authMigrationConfigService.findByStudyEnvId(studyEnvironment.getId());
        if (authMigrationConfigOpt.isEmpty()) {
            log.error("No auth migration config found for study environment ID: {}", studyEnvironment.getId());
            return false;
        }
        return this.attemptLegacyLogIn(authMigrationConfigOpt.get(), email, password);
    }

    public boolean attemptLegacyLogIn(
            AuthMigrationConfig config,
            String email,
            String password
    )  {
        LegacyAuthProviderService providerService = getLegacyAuthProviderService(config);
        return providerService.attemptLogIn(config, email, password);
    }

    private LegacyAuthProviderService getLegacyAuthProviderService(AuthMigrationConfig config) {
        return switch (config.getAuthProvider()) {
            case AUTH0 -> auth0ProviderService;
            default -> throw new IllegalArgumentException(
                    "Unsupported auth provider: " + config.getAuthProvider());
        };
    }


}
