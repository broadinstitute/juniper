package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import bio.terra.pearl.core.shared.ParticipantUiConstants;
import java.util.Map;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** handles template replacement */
public class EnrolleeEmailSubstitutor implements StringLookup {
    private static final Logger logger = LoggerFactory.getLogger(StringLookup.class);
    private Map<String, Object> valueMap;
    private EnrolleeRuleData enrolleeRuleData;
    private PortalEnvironment portalEnvironment;

    protected EnrolleeEmailSubstitutor(EnrolleeRuleData ruleData, PortalEnvironment portalEnv, String portalShortcode) {
        this.enrolleeRuleData = ruleData;
        this.portalEnvironment = portalEnv;
        valueMap = Map.of("profile", enrolleeRuleData.getProfile(),
                "portalEnv", portalEnv,
                "envConfig", portalEnv.getPortalEnvironmentConfig(),
                "dashboardLink", getDashboardLink(portalEnv, portalShortcode));
    }

    /** create a new substitutor.  the portalEnv must have the envConfig attached */
    public static StringSubstitutor newSubstitutor(EnrolleeRuleData ruleData,
                                                   PortalEnvironment portalEnv, String portalShortcode) {
        return new StringSubstitutor(new EnrolleeEmailSubstitutor(ruleData, portalEnv, portalShortcode));
    }


    @Override
    public String lookup(String key) {
        try {
            return PropertyUtils.getNestedProperty(valueMap, key).toString();
        } catch (Exception e) {
            logger.error("Could not resolve template value {}, environment: {}, enrollee: {}",
                    key, portalEnvironment.getId(), enrolleeRuleData.getEnrollee().getShortcode());
        }
        return "";
    }

    public String getDashboardLink(PortalEnvironment portalEnv, String portalShortcode) {
        String href = getParticipantHostname(portalEnv, portalShortcode) + ParticipantUiConstants.DASHBOARD_PATH;
        String dashLink = String.format("<a href=\"%s\">Go to dashboard</a>", href);
        logger.info("DASHLINK: {}", dashLink);
        return String.format("<a href=\"%s\">Go to dashboard</a>", href);
    }

    public static String getParticipantHostname(PortalEnvironment portalEnv, String portalShortcode) {
        String participantHostname = portalEnv.getPortalEnvironmentConfig().getParticipantHostname();
        if (participantHostname == null) {
            // TODO read from environment variable once Mike adds support for it
            participantHostname = portalShortcode + "." + ParticipantUiConstants.LOCAL_DEV_HOSTNAME;
        }

        if (!portalEnv.getEnvironmentName().isProduction()) {
            participantHostname = portalEnv.getEnvironmentName() + "." + participantHostname;
        }
        if (!participantHostname.contains("localhost")) {
            participantHostname = "https://" + participantHostname;
        } else {
            participantHostname = "http://" + participantHostname;
        }
        return participantHostname;
    }
}
