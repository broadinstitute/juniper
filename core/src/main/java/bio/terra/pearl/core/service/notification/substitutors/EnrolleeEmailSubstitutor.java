package bio.terra.pearl.core.service.notification.substitutors;

import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
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
        valueMap.put("profile", enrolleeRuleData.getProfile());
        valueMap.put("portalEnv", contextInfo.portalEnv());
        valueMap.put("envConfig", contextInfo.portalEnvConfig());
        valueMap.put("dashboardLink", getDashboardLink(contextInfo.portalEnv(), contextInfo.portalEnvConfig(), contextInfo.portal(), contextInfo.study()));
        valueMap.put("siteLink", getSiteLink(contextInfo.portalEnv(), contextInfo.portalEnvConfig(), contextInfo.portal()));
        valueMap.put("participantSupportEmailLink", getParticipantSupportEmailLink(contextInfo.portalEnv(), contextInfo.portalEnvConfig()));
        valueMap.put("siteMediaBaseUrl", getImageBaseUrl(contextInfo.portalEnv(), contextInfo.portalEnvConfig(), contextInfo.portal().getShortcode()));
        valueMap.put("siteImageBaseUrl", getImageBaseUrl(contextInfo.portalEnv(), contextInfo.portalEnvConfig(), contextInfo.portal().getShortcode()));
        valueMap.put("profile", enrolleeRuleData.getProfile());
        valueMap.put("study", contextInfo.study());
        valueMap.put("participantUser", ruleData.getParticipantUser());
        valueMap.put("invitationLink", getInvitationLink(contextInfo.portalEnv(), contextInfo.portalEnvConfig(), contextInfo.portal().getShortcode(), ruleData.getParticipantUser()));
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
                    key, contextInfo.portal().getShortcode(), enrolleeRuleData.getEnrollee().getShortcode());
        }
        return "";
    }

    public String getSiteLink(PortalEnvironment portalEnv, PortalEnvironmentConfig config, Portal portal) {
        String href = routingPaths.getParticipantBaseUrl(portalEnv, config, portal.getShortcode());
        return String.format("<a rel=\"noopener\" href=\"%s\" target=\"_blank\">%s</a>", href, href);
    }


    public String getDashboardLink(PortalEnvironment portalEnv, PortalEnvironmentConfig config, Portal portal, Study study) {
        String href = getDashboardUrl(portalEnv, config, portal);
        String linkNameText = study != null ? study.getName() : portal.getName();
        return String.format("<a href=\"%s\">Return to %s</a>", href, linkNameText);
    }

    public String getDashboardUrl(PortalEnvironment portalEnv, PortalEnvironmentConfig config, Portal portal) {
        return routingPaths.getParticipantBaseUrl(portalEnv, config, portal.getShortcode()) +
                routingPaths.getParticipantDashboardPath();
    }

    public String getImageBaseUrl(PortalEnvironment portalEnv, PortalEnvironmentConfig config, String portalShortcode) {
        return routingPaths.getParticipantBaseUrl(portalEnv, config, portalShortcode)
                + "/api/public/portals/v1/" + portalShortcode + "/env/" + portalEnv.getEnvironmentName()
                + "/siteMedia";
    }

    public String getParticipantSupportEmailLink(PortalEnvironment portalEnvironment, PortalEnvironmentConfig config) {
        String emailAddress = config.getEmailSourceAddress();
        if (StringUtils.isBlank(emailAddress)) {
            // if there's nothing configured for the study, default to the site-wide Juniper support email
            emailAddress = routingPaths.getSupportEmailAddress();
        }
        return String.format("<a href=\"mailto:%s\" rel=\"noopener\" target=\"_blank\">%s</a>", emailAddress, emailAddress);
    }

    /** gets a link the participant can use to create their b2c account, given that they already exist in Juniper */
    public String getInvitationLink(PortalEnvironment portalEnv, PortalEnvironmentConfig config, String portalShortcode, ParticipantUser participantUser) {
        return "%s%s?accountName=%s".formatted(
                routingPaths.getParticipantBaseUrl(portalEnv, config, portalShortcode),
                routingPaths.getParticipantInvitationPath() ,
                participantUser != null ? participantUser.getUsername() : "");
    }
}
