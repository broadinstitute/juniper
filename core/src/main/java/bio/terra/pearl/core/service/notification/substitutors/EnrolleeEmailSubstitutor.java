package bio.terra.pearl.core.service.notification.substitutors;

import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.service.notification.NotificationContextInfo;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import bio.terra.pearl.core.shared.ApplicationRoutingPaths;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;

/** handles template replacement.  Note that this class is not a Spring component since a separate instance should be created
 * for each email to be sent. */
@Slf4j
public class EnrolleeEmailSubstitutor implements StringLookup {
    private final Map<String, Object> valueMap = new HashMap<>();
    private EnrolleeRuleData enrolleeRuleData;
    private NotificationContextInfo contextInfo;
    private final ApplicationRoutingPaths routingPaths;

    protected EnrolleeEmailSubstitutor(EnrolleeRuleData ruleData, NotificationContextInfo contextInfo,
                                       ApplicationRoutingPaths routingPaths, Map<String, String> messages) {
        this.enrolleeRuleData = ruleData;
        this.contextInfo = contextInfo;
        this.routingPaths = routingPaths;
        valueMap.putAll(Map.of("profile", enrolleeRuleData.profile(),
                "portalEnv", contextInfo.portalEnv(),
                "envConfig", contextInfo.portalEnv().getPortalEnvironmentConfig(),
                "dashboardLink", getDashboardLink(contextInfo.portalEnv(),
                        contextInfo.portal(), contextInfo.study()),
                "dashboardUrl", getDashboardUrl(contextInfo.portalEnv(), contextInfo.portal()),
                "siteLink", getSiteLink(contextInfo.portalEnv(), contextInfo.portal()),
                "participantSupportEmailLink", getParticipantSupportEmailLink(contextInfo.portalEnv()),
                "siteImageBaseUrl", getImageBaseUrl(contextInfo.portalEnv(), contextInfo.portal().getShortcode()),
                // providing a study isn't required, since emails might come from the portal, rather than a study
                // but immutable map doesn't allow nulls
                "study", contextInfo.study() != null ? contextInfo.study() : ""));
        if (messages != null) {
            valueMap.putAll(messages);
        }
    }

    /** create a new substitutor.  the portalEnv must have the envConfig attached */
    public static StringSubstitutor newSubstitutor(EnrolleeRuleData ruleData,
                                                   NotificationContextInfo contextInfo,
                                                   ApplicationRoutingPaths routingPaths) {
        return new StringSubstitutor(new EnrolleeEmailSubstitutor(ruleData, contextInfo, routingPaths, null));
    }

    public static StringSubstitutor newSubstitutor(EnrolleeRuleData ruleData,
                                                   NotificationContextInfo contextInfo,
                                                   ApplicationRoutingPaths routingPaths,
                                                   Map<String, String> customMessages) {
        return new StringSubstitutor(new EnrolleeEmailSubstitutor(ruleData, contextInfo, routingPaths, customMessages));
    }



    @Override
    public String lookup(String key) {
        try {
            return PropertyUtils.getNestedProperty(valueMap, key).toString();
        } catch (Exception e) {
            log.error("Could not resolve template value {}, environment: {}, enrollee: {}",
                    key, contextInfo.portal().getShortcode(), enrolleeRuleData.enrollee().getShortcode());
        }
        return "";
    }

    public String getSiteLink(PortalEnvironment portalEnv, Portal portal) {
        String href = routingPaths.getParticipantBaseUrl(portalEnv, portal.getShortcode());
        return String.format("<a rel=\"noopener\" href=\"%s\" target=\"_blank\">%s</a>", href, href);
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

    public String getParticipantSupportEmailLink(PortalEnvironment portalEnvironment) {
        String emailAddress =portalEnvironment.getPortalEnvironmentConfig().getEmailSourceAddress();
        if (StringUtils.isBlank(emailAddress)) {
            // if there's nothing configured for the study, default to the site-wide Juniper support email
            emailAddress = routingPaths.getSupportEmailAddress();
        }
        return String.format("<a href=\"mailto:%s\" rel=\"noopener\" target=\"_blank\">%s</a>", emailAddress, emailAddress);
    }


}
