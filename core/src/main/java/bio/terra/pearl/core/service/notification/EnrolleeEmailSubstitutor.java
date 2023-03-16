package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.Study;
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
    private NotificationContextInfo contextInfo;

    protected EnrolleeEmailSubstitutor(EnrolleeRuleData ruleData, NotificationContextInfo contextInfo) {
        this.enrolleeRuleData = ruleData;
        this.contextInfo = contextInfo;
        valueMap = Map.of("profile", enrolleeRuleData.profile(),
                "portalEnv", contextInfo.portalEnv(),
                "envConfig", contextInfo.portalEnv().getPortalEnvironmentConfig(),
                "dashboardLink", getDashboardLink(contextInfo.portalEnv(),
                        contextInfo.portal(), contextInfo.study()),
                // providing a study isn't required, since emails might come from the portal, rather than a study
                // but immutable map doesn't allow nulls
                "study", contextInfo.study() != null ? contextInfo.study() : "");
    }

    /** create a new substitutor.  the portalEnv must have the envConfig attached */
    public static StringSubstitutor newSubstitutor(EnrolleeRuleData ruleData,
                                                   NotificationContextInfo contextInfo) {
        return new StringSubstitutor(new EnrolleeEmailSubstitutor(ruleData, contextInfo));
    }


    @Override
    public String lookup(String key) {
        try {
            return PropertyUtils.getNestedProperty(valueMap, key).toString();
        } catch (Exception e) {
            logger.error("Could not resolve template value {}, environment: {}, enrollee: {}",
                    key, contextInfo.portal().getShortcode(), enrolleeRuleData.enrollee().getShortcode());
        }
        return "";
    }

    public String getDashboardLink(PortalEnvironment portalEnv, Portal portal,  Study study) {
        String href = getParticipantHostname(portalEnv, portal.getShortcode()) + ParticipantUiConstants.DASHBOARD_PATH;
        String linkNameText = study != null ? study.getName() : portal.getName();
        return String.format("<a href=\"%s\">Return to %s</a>", href, linkNameText);
    }

    public static String getParticipantHostname(PortalEnvironment portalEnv, String portalShortcode) {
        String participantHostname = portalEnv.getPortalEnvironmentConfig().getParticipantHostname();
        if (participantHostname == null) {
            // TODO read from environment variable once Mike adds support for it
            participantHostname = portalShortcode + "." + ParticipantUiConstants.LOCAL_DEV_HOSTNAME;
        }

        if (!portalEnv.getEnvironmentName().isLive()) {
            participantHostname = portalEnv.getEnvironmentName() + "." + participantHostname;
        }
        participantHostname = "https://" + participantHostname;
        return participantHostname;
    }
}
