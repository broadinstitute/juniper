package bio.terra.pearl.core.shared;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
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
    @Getter
    private final String participantInvitationPath = "/join/invitation";
    @Getter
    private final String supportEmailAddress;  // the site-wide support email address (e.g. support@juniper...) NOT study-specific
    @Getter
    private final String deploymentZone; // demo|prod|local

    public ApplicationRoutingPaths(Environment env) {
        participantUiHostname = env.getProperty("env.hostnames.participantUi");
        participantApiHostname = env.getProperty("env.hostnames.participantApi");
        adminUiHostname = env.getProperty("env.hostnames.adminUi");
        adminApiHostname = env.getProperty("env.hostnames.adminApi");
        supportEmailAddress = env.getProperty("env.email.supportEmailAddress");
        deploymentZone = env.getProperty("env.deploymentZone", "local");
    }

    public String getAdminBaseUrl() {
        return "https://" + adminUiHostname;
    }

    public String getAdminPortalUrl(String portalShortcode) {
        return "https://" + adminUiHostname + "/" + portalShortcode;
    }

    public String getAdminStudyUrl(String portalShortcode, String studyShortcode) {
        return getAdminPortalUrl(portalShortcode) + "/studies/" + studyShortcode;
    }

    public String getAdminStudyEnvUrl(String portalShortcode, String studyShortcode, EnvironmentName environment) {
        return getAdminStudyUrl(portalShortcode, studyShortcode) + "/env/" + environment.toString().toLowerCase();
    }

    public String getParticipantBaseUrl(PortalEnvironment portalEnv, PortalEnvironmentConfig config, String portalShortcode) {
        String participantHostname = config.getParticipantHostname();
        if (participantHostname == null) {
            participantHostname = portalShortcode + "." + getParticipantUiHostname();
        }

        if (!portalEnv.getEnvironmentName().isLive()) {
            participantHostname = portalEnv.getEnvironmentName() + "." + participantHostname;
        }
        participantHostname = "https://" + participantHostname;
        return participantHostname;
    }
}
