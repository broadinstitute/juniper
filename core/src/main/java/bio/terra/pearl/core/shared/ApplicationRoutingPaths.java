package bio.terra.pearl.core.shared;

import bio.terra.pearl.core.model.portal.PortalEnvironment;
import lombok.Getter;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/** Routing info that need to be known across the entire app, so that, e.g., the app can generate emails with links */
@Component
public class ApplicationRoutingPaths {
    @Getter
    private final String participantUiHostname;
    @Getter
    private final String participantApiHostname;
    @Getter
    private final String adminApiHostname;
    @Getter
    private final String adminUiHostname;
    @Getter
    private final String participantDashboardPath = "/hub";

    public ApplicationRoutingPaths(Environment env) {
        // we don't want to defaul
        participantUiHostname = env.getProperty("env.hostnames.participantUi");
        participantApiHostname = env.getProperty("env.hostnames.participantApi");
        adminUiHostname = env.getProperty("env.hostnames.adminUi");
        adminApiHostname = env.getProperty("env.hostnames.adminApi");
    }

    public String getParticipantBaseUrl(PortalEnvironment portalEnv, String portalShortcode) {
        String participantHostname = portalEnv.getPortalEnvironmentConfig().getParticipantHostname();
        if (participantHostname == null) {
            // TODO read from environment variable once Mike adds support for it
            participantHostname = portalShortcode + "." + getParticipantUiHostname();
        }

        if (!portalEnv.getEnvironmentName().isLive()) {
            participantHostname = portalEnv.getEnvironmentName() + "." + participantHostname;
        }
        participantHostname = "https://" + participantHostname;
        return participantHostname;
    }
}
