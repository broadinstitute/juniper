package bio.terra.pearl.core.service.logging;

import com.mixpanel.mixpanelapi.ClientDelivery;
import com.mixpanel.mixpanelapi.MessageBuilder;
import com.mixpanel.mixpanelapi.MixpanelAPI;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class MixpanelService {
    private final Environment env;

    public MixpanelService(Environment env) {
        this.env = env;
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
        if(!Boolean.parseBoolean(env.getProperty("env.mixpanel.enabled"))) {
            return;
        }

        // Filter all the incoming events in one pass, so we don't have
        // to unpack the JSONObject and repack it for each individual event
        String filteredData = filterEventData(data);
        
        //Mixpanel sends event data as urlencoded form data, so we need to parse the event data as a JSON array
        JSONArray events = new JSONArray(filteredData);

        ClientDelivery delivery = new ClientDelivery();

        for (int i = 0; i < events.length(); i++) {
            JSONObject mixpanelEvent = buildEvent(events.getJSONObject(i));
            delivery.addMessage(mixpanelEvent);
        }

        deliverEvents(delivery);
    }

    protected JSONObject buildEvent(JSONObject event) {
        MessageBuilder messageBuilder = new MessageBuilder(env.getProperty("mixpanel.token"));

        return messageBuilder.event(
                null,
                event.getString("event"),
                event.getJSONObject("properties")
                        .put("token", env.getProperty("mixpanel.token"))
        );
    }

    protected void deliverEvents(ClientDelivery delivery) {
        MixpanelAPI mixpanel = new MixpanelAPI();

        try {
            mixpanel.deliver(delivery);
        } catch (IOException e) {
            log.info("Failed to deliver event to Mixpanel: {}", e.getMessage());
        }
    }

}
