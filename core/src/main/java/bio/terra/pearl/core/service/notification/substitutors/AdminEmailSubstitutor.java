package bio.terra.pearl.core.service.notification.substitutors;

import bio.terra.pearl.core.service.notification.NotificationContextInfo;
import bio.terra.pearl.core.shared.ApplicationRoutingPaths;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;

/** handles template replacement.  Note that this class is not a Spring component since a separate instance should be created
 * for each email to be sent. */
@Slf4j
public class AdminEmailSubstitutor implements StringLookup {
  private Map<String, Object> valueMap;
  private final ApplicationRoutingPaths routingPaths;

  protected AdminEmailSubstitutor(String adminUsername, NotificationContextInfo contextInfo,
                                     ApplicationRoutingPaths routingPaths) {
    this.routingPaths = routingPaths;
    valueMap = Map.of("adminUsername", adminUsername,
        "loginLink", getLoginLink(),
        "supportEmail", routingPaths.getSupportEmailAddress() == null ? "" : routingPaths.getSupportEmailAddress());
  }

  /** create a new substitutor.  the portalEnv must have the envConfig attached */
  public static StringSubstitutor newSubstitutor(String adminUsername,
                                                 NotificationContextInfo contextInfo,
                                                 ApplicationRoutingPaths routingPaths) {
    return new StringSubstitutor(new AdminEmailSubstitutor(adminUsername, contextInfo, routingPaths));
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
}
