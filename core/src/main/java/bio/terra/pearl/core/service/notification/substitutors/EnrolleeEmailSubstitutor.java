package bio.terra.pearl.core.service.notification.substitutors;

import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.service.notification.NotificationContextInfo;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import bio.terra.pearl.core.shared.ApplicationRoutingPaths;
import java.util.Map;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** handles template replacement.  Note that this class is not a Spring component since a separate instance should be created
 * for each email to be sent. */
public class EnrolleeEmailSubstitutor implements StringLookup {
    private static final Logger logger = LoggerFactory.getLogger(EnrolleeEmailSubstitutor.class);
    private Map<String, Object> valueMap;
    private EnrolleeRuleData enrolleeRuleData;
    private NotificationContextInfo contextInfo;
    private final ApplicationRoutingPaths routingPaths;

    protected EnrolleeEmailSubstitutor(EnrolleeRuleData ruleData, NotificationContextInfo contextInfo,
                                       ApplicationRoutingPaths routingPaths) {
        this.enrolleeRuleData = ruleData;
        this.contextInfo = contextInfo;
        this.routingPaths = routingPaths;
        valueMap = Map.of("profile", enrolleeRuleData.profile(),
                "portalEnv", contextInfo.portalEnv(),
                "envConfig", contextInfo.portalEnv().getPortalEnvironmentConfig(),
                "dashboardLink", getDashboardLink(contextInfo.portalEnv(),
                        contextInfo.portal(), contextInfo.study()),
                "dashboardUrl", getDashboardUrl(contextInfo.portalEnv(), contextInfo.portal()),
                "siteImageBaseUrl", getImageBaseUrl(contextInfo.portalEnv(), contextInfo.portal().getShortcode()),
                // providing a study isn't required, since emails might come from the portal, rather than a study
                // but immutable map doesn't allow nulls
                "study", contextInfo.study() != null ? contextInfo.study() : "");
    }

    /** create a new substitutor.  the portalEnv must have the envConfig attached */
    public static StringSubstitutor newSubstitutor(EnrolleeRuleData ruleData,
                                                   NotificationContextInfo contextInfo,
                                                   ApplicationRoutingPaths routingPaths) {
        return new StringSubstitutor(new EnrolleeEmailSubstitutor(ruleData, contextInfo, routingPaths));
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

    public String getDashboardLink(PortalEnvironment portalEnv, Portal portal, Study study) {
        String href = getDashboardUrl(portalEnv, portal);
        String linkNameText = study != null ? study.getName() : portal.getName();
        return String.format("<a href=\"%s\">Return to %s</a>", href, linkNameText);
    }

    public String getDashboardUrl(PortalEnvironment portalEnv, Portal portal) {
        return routingPaths.getParticipantBaseUrl(portalEnv, portal.getShortcode()) +
                routingPaths.getParticipantDashboardPath();
    }

    public String getImageBaseUrl(PortalEnvironment portalEnvironment, String portalShortcode) {
        return routingPaths.getParticipantBaseUrl(portalEnvironment, portalShortcode)
                + "/api/public/portals/v1/" + portalShortcode + "/env/" + portalEnvironment.getEnvironmentName()
                + "/siteImages";
    }


}
