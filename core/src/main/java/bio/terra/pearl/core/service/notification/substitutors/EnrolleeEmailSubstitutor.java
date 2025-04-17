package bio.terra.pearl.core.service.notification.substitutors;

import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.RelationshipType;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.service.exception.internal.IOInternalException;
import bio.terra.pearl.core.service.notification.NotificationContextInfo;
import bio.terra.pearl.core.service.rule.EnrolleeContext;
import bio.terra.pearl.core.shared.ApplicationRoutingPaths;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/** handles template replacement.  Note that this class is not a Spring component since a separate instance should be created
 * for each email to be sent. */
@Slf4j
public class EnrolleeEmailSubstitutor implements StringLookup {
    private final Map<String, Object> valueMap = new HashMap<>();
    private EnrolleeContext enrolleeContext;
    private NotificationContextInfo contextInfo;
    private final ApplicationRoutingPaths routingPaths;

    protected EnrolleeEmailSubstitutor(EnrolleeContext ruleData,
                                       NotificationContextInfo contextInfo,
                                       ApplicationRoutingPaths routingPaths,
                                       Map<String, String> messages) {
        this.enrolleeContext = ruleData;
        this.contextInfo = contextInfo;
        this.routingPaths = routingPaths;
        valueMap.put("profile", enrolleeContext.getProfile());
        valueMap.put("portalEnv", contextInfo.portalEnv());
        valueMap.put("envConfig", contextInfo.portalEnvConfig());
        valueMap.put("dashboardLink", getDashboardLink(contextInfo.portalEnv(), contextInfo.portalEnvConfig(), contextInfo.portal(), contextInfo.study()));
        valueMap.put("dashboardUrl", getDashboardUrl(contextInfo.portalEnv(), contextInfo.portalEnvConfig(), contextInfo.portal()));
        valueMap.put("siteLink", getSiteLink(contextInfo.portalEnv(), contextInfo.portalEnvConfig(), contextInfo.portal()));
        valueMap.put("participantSupportEmailLink", getParticipantSupportEmailLink(contextInfo.portalEnv(), contextInfo.portalEnvConfig()));
        valueMap.put("siteMediaBaseUrl", getImageBaseUrl(contextInfo.portalEnv(), contextInfo.portalEnvConfig(), contextInfo.portal().getShortcode()));
        valueMap.put("siteImageBaseUrl", getImageBaseUrl(contextInfo.portalEnv(), contextInfo.portalEnvConfig(), contextInfo.portal().getShortcode()));
        boolean isProxy = isProxy(ruleData);
        if (isProxy) {
            valueMap.put("isProxy", "true");
        } else {
            valueMap.put("isProxy", "false");
        }
        valueMap.put("enrollee", ruleData.getEnrollee());
        valueMap.put("study", contextInfo.study());
        valueMap.put("participantUser", ruleData.getParticipantUser());
        valueMap.put("invitationLink", getInvitationLink(contextInfo.portalEnv(), contextInfo.portalEnvConfig(), contextInfo.portal().getShortcode(), ruleData.getParticipantUser(), isProxy));
        if (messages != null) {
            valueMap.putAll(messages);
        }
    }

    private boolean isProxy(EnrolleeContext context) {
        if (context.getRelations() == null) {
            return false;
        }

        return context
                .getRelations()
                .stream()
                .anyMatch(relation -> relation.getRelationshipType().equals(RelationshipType.PROXY)
                        && relation.getTargetEnrolleeId().equals(context.getEnrollee().getId()));
    }

    /** create a new substitutor.  the portalEnv must have the envConfig attached */
    public static StringSubstitutor newSubstitutor(EnrolleeContext ruleData,
                                                   NotificationContextInfo contextInfo,
                                                   ApplicationRoutingPaths routingPaths) {
        return new StringSubstitutor(new EnrolleeEmailSubstitutor(ruleData, contextInfo, routingPaths, null));
    }

    public static StringSubstitutor newSubstitutor(EnrolleeContext ruleData,
                                                   NotificationContextInfo contextInfo,
                                                   ApplicationRoutingPaths routingPaths,
                                                   Map<String, String> customMessages) {
        return new StringSubstitutor(new EnrolleeEmailSubstitutor(ruleData, contextInfo, routingPaths, customMessages));
    }

    @Override
    public String lookup(String key) {
        try {
            if (Ternary.isTernary(key)) {
                return lookupTernary(key);
            }

            return PropertyUtils.getNestedProperty(valueMap, key).toString();
        } catch (Exception e) {
            log.error("Could not resolve template value {}, environment: {}, enrollee: {}",
                    key, contextInfo.portal().getShortcode(), enrolleeContext.getEnrollee().getShortcode());
        }
        return "";
    }


    // matches a ternary expression, e.g. "isProxy ? "yes" : "no""
    final static Pattern isTernaryPattern = Pattern.compile("[^:]+\\?.+:.+");
    private record Ternary(String condition, String left, String right) {
        public static Ternary fromString(String key) {
            String[] parts = key.split("\\?");
            String condition = parts[0];
            String[] values = parts[1].split(":");
            return new Ternary(condition.trim(), values[0].trim(), values[1].trim());
        }


        public static boolean isTernary(String key) {
            return isTernaryPattern.matcher(key).matches();
        }
    }

    private String lookupTernary(String key) {
        Ternary ternary = Ternary.fromString(key);

        if (lookup(ternary.condition).equals("true")) {
            return parseAsStringOrLookup(ternary.left);
        } else {
            return parseAsStringOrLookup(ternary.right);
        }
    }

    private String parseAsStringOrLookup(String variableOrString) {
        if (variableOrString.startsWith("\"") && variableOrString.endsWith("\"")) {
            return variableOrString.substring(1, variableOrString.length() - 1);
        }

        return lookup(variableOrString);
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

    /**
     * gets a link the participant can use to create their b2c account, given that they already exist in Juniper
     */
    public String getInvitationLink(PortalEnvironment portalEnv, PortalEnvironmentConfig config, String portalShortcode, ParticipantUser participantUser, boolean isProxy) {
        try {
            String username = isProxy
                    ? removeProxySuffix(participantUser.getUsername())
                    : participantUser.getUsername();
            return "%s%s?accountName=%s".formatted(
                    routingPaths.getParticipantBaseUrl(portalEnv, config, portalShortcode),
                    routingPaths.getParticipantInvitationPath(),
                    participantUser != null ?
                            URLEncoder.encode(
                                    username,
                                    StandardCharsets.UTF_8.toString()) : "");
        } catch (UnsupportedEncodingException e) {
            throw new IOInternalException("unable to encode username");
        }
    }

    private String removeProxySuffix(String email) {
        // if the email ends with -prox-XXXX, remove it
        Pattern proxySuffix = Pattern.compile("-prox-[A-Z]{4}$");

        return proxySuffix.matcher(email).replaceAll("");
    }
}
