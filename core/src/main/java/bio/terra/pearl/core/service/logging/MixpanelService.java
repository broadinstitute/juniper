package bio.terra.pearl.core.service.logging;

import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import com.mixpanel.mixpanelapi.ClientDelivery;
import com.mixpanel.mixpanelapi.MessageBuilder;
import com.mixpanel.mixpanelapi.MixpanelAPI;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class MixpanelService {
    private static final String MIXPANEL_TOKEN_ENV_VAR = "env.mixpanel.token";
    private static final String MIXPANEL_ENABLED_ENV_VAR = "env.mixpanel.enabled";
    private final MixpanelConfig mixpanelConfig;
    private final LoggingConfigCache loggingConfigCache;

    public MixpanelService(MixpanelConfig mixpanelConfig, LoggingConfigCache loggingConfigCache) {
        this.mixpanelConfig = mixpanelConfig;
        this.loggingConfigCache = loggingConfigCache;
    }

    private Map<String, String> getRedactionPatterns() {
        Map<String, String> patterns = new HashMap<>();
        patterns.put("([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})", "{REDACTED_EMAIL}");
        // This pattern matches phone numbers in the format (123)-456-7890 or 123-456-7890
        // It does not match phone numbers in the format 1234567890, as this unfortunately
        // would require more sophistication to avoid redacting timestamps
        patterns.put("\\(?[0-9]{3}\\)?-[0-9]{3}-[0-9]{4}", "{REDACTED_PHONE_NUMBER}");
        return patterns;
    }

    // This method is used to redact any sensitive information from the event data
    // before sending it to Mixpanel. Currently, this only redacts email addresses
    // and phone numbers, but can be expanded to redact other sensitive information
    public String filterEventData(String data) {
        String filteredData = data;
        for (Map.Entry<String, String> entry : getRedactionPatterns().entrySet()) {
            filteredData = filteredData.replaceAll(entry.getKey(), entry.getValue());
        }
        return filteredData;
    }

    public void logEvent(String data) {
        if(mixpanelConfig.enabled) {
            return;
        }

        // Filter all the incoming events in one pass, so we don't have
        // to unpack the JSONObject and repack it for each individual event
        String filteredData = filterEventData(data);

        //Mixpanel sends event data as urlencoded form data, so we need to parse the event data as a JSON array
        JSONArray events = new JSONArray(filteredData);

        ClientDelivery delivery = new ClientDelivery();

        for (int i = 0; i < events.length(); i++) {
            JSONObject event = events.getJSONObject(i);
            JSONObject mixpanelEvent = buildEvent(event, mixpanelConfig.token);
            delivery.addMessage(mixpanelEvent);
            String eventDomain = getEventCurrentDomain(event);

            /** check if the event comes from a domain with a dedicated mixpanel token, if so, use that token to send the
             * event to that token in addition to the global mixpanel domain */
            if (eventDomain != null) {
                Map<String, PortalEnvironmentConfig> configMap = loggingConfigCache.getConfigsWithDomain();
                 PortalEnvironmentConfig matchedConfig = configMap.get(eventDomain);
                if (matchedConfig != null && matchedConfig.getMixpanelToken() != null) {
                    JSONObject domainEvent = buildEvent(event, matchedConfig.getMixpanelToken());
                    delivery.addMessage(domainEvent);
                }
            }
        }

        deliverEvents(delivery);
    }

    protected JSONObject buildEvent(JSONObject event, String apiToken) {

        MessageBuilder messageBuilder = new MessageBuilder(apiToken);

        return messageBuilder.event(
                null,
                event.getString("event"),
                event.getJSONObject("properties")
                        .put("token", apiToken)
        );
    }

    protected void deliverEvents(ClientDelivery delivery) {
        MixpanelAPI mixpanel = new MixpanelAPI();
        if(!mixpanelConfig.enabled) {
            return;
        }
        try {
            mixpanel.deliver(delivery);
        } catch (IOException e) {
            log.info("Failed to deliver event to Mixpanel: {}", e.getMessage());
        }
    }

    protected String getEventCurrentDomain(JSONObject event) {
        String domain = null;
        if (event.has("properties")) {
            JSONObject properties = event.getJSONObject("properties");
            if (properties.has("$current_url")) {
                String url = properties.getString("$current_url");
                return getDomainName(url);
            }
        }
        return domain;
    }

    public static String getDomainName(String url) {
        try {
            URI uri = new URI(url);
            String domain = uri.getHost();
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        } catch (Exception e) {
            return null;
        }
    }

    @Component
    @Getter @Setter
    public static class MixpanelConfig {
        private String token;
        private Boolean enabled;

        public MixpanelConfig(Environment environment) {
            this.token = environment.getProperty("env.mixpanel.token");
            this.enabled = Boolean.parseBoolean(environment.getProperty("env.mixpanel.enabled"));
        }
    }




}
