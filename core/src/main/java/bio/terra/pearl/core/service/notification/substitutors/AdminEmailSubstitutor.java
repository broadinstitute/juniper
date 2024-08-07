package bio.terra.pearl.core.service.notification.substitutors;

import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.service.notification.NotificationContextInfo;
import bio.terra.pearl.core.service.rule.EnrolleeContext;
import bio.terra.pearl.core.shared.ApplicationRoutingPaths;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** handles template replacement.  Note that this class is not a Spring component since a separate instance should be created
 * for each email to be sent. */
@Slf4j
public class AdminEmailSubstitutor implements StringLookup {
  private Map<String, Object> valueMap;
  private final ApplicationRoutingPaths routingPaths;

  protected AdminEmailSubstitutor(String adminUsername,
                                  NotificationContextInfo contextInfo,
                                  ApplicationRoutingPaths routingPaths,
                                  EnrolleeContext enrolleeContext) {
    this.routingPaths = routingPaths;
    valueMap = new HashMap<>();
    valueMap.put("adminUsername", adminUsername);
    valueMap.put("loginLink", getLoginLink());
    valueMap.put("supportEmail", routingPaths.getSupportEmailAddress() == null ? "" : routingPaths.getSupportEmailAddress());

    if (Objects.nonNull(enrolleeContext)) {
      valueMap.put("enrollee", enrolleeContext.getEnrollee());
      valueMap.put("profile", enrolleeContext.getProfile());
      valueMap.put("participantUser", enrolleeContext.getParticipantUser());
      valueMap.put("enrolleeUrl", getEnrolleeUrl(contextInfo, enrolleeContext.getEnrollee().getShortcode()));
    }

    if (Objects.nonNull(contextInfo.portalEnv()) && Objects.nonNull(contextInfo.portalEnvConfig()) && Objects.nonNull(contextInfo.portal())) {
      valueMap.put("siteMediaBaseUrl", getImageBaseUrl(contextInfo.portalEnv(), contextInfo.portalEnvConfig(), contextInfo.portal().getShortcode()));
      valueMap.put("siteImageBaseUrl", getImageBaseUrl(contextInfo.portalEnv(), contextInfo.portalEnvConfig(), contextInfo.portal().getShortcode()));
    }
  }

  /**
   * create a new substitutor. if the email template is about a specific enrollee,
   * enrolleecontext should be provided, otherwise it can be null.
   * the portalEnv must have the envConfig attached
   */
  public static StringSubstitutor newSubstitutor(String adminUsername,
                                                 NotificationContextInfo contextInfo,
                                                 ApplicationRoutingPaths routingPaths,
                                                 EnrolleeContext enrolleeContext) {
    return new StringSubstitutor(new AdminEmailSubstitutor(adminUsername, contextInfo, routingPaths, enrolleeContext));
  }


  @Override
  public String lookup(String key) {
    try {
      return PropertyUtils.getNestedProperty(valueMap, key).toString();
    } catch (Exception e) {
      log.error("Could not resolve template value {}", key);
    }
    return "";
  }

  public String getLoginLink() {
    String href = routingPaths.getAdminBaseUrl();
    return String.format("<a href=\"%s\">Login to Juniper</a>", href);
  }

  public String getEnrolleeUrl(NotificationContextInfo contextInfo, String enrolleeShortcode) {
    return routingPaths.getAdminStudyEnvUrl(
            contextInfo.portal().getShortcode(),
            contextInfo.study().getShortcode(),
            contextInfo.portalEnv().getEnvironmentName()) + "/participants/" + enrolleeShortcode;
  }

  public String getImageBaseUrl(PortalEnvironment portalEnv, PortalEnvironmentConfig config, String portalShortcode) {
    return routingPaths.getParticipantBaseUrl(portalEnv, config, portalShortcode)
            + "/api/public/portals/v1/" + portalShortcode + "/env/" + portalEnv.getEnvironmentName()
            + "/siteMedia";
  }
}
