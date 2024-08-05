package bio.terra.pearl.core.service.events;

import com.mixpanel.mixpanelapi.ClientDelivery;
import com.mixpanel.mixpanelapi.MessageBuilder;
import com.mixpanel.mixpanelapi.MixpanelAPI;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class MixpanelService {
    private final Environment env;

    public MixpanelService(Environment env) {
        this.env = env;
    }

    public void logEvent(String data) {
        if(!Boolean.parseBoolean(env.getProperty("env.mixpanel.enabled"))) {
            log.info("Mixpanel is disabled. Skipping event logging.");
            return;
        }

        //Mixpanel sends event data as urlencoded form data, so we need to parse the event data as a JSON array
        JSONArray events = new JSONArray(data);

        ClientDelivery delivery = new ClientDelivery();
        MessageBuilder messageBuilder = new MessageBuilder(env.getProperty("mixpanel.token"));

        for (int i = 0; i < events.length(); i++) {
            JSONObject event = events.getJSONObject(i);
            JSONObject updatedEvent = messageBuilder.event(
                    null,
                    event.getString("event"),
                    event.getJSONObject("properties")
                            .put("token", env.getProperty("mixpanel.token"))
            );

            delivery.addMessage(updatedEvent);
        }

        MixpanelAPI mixpanel = new MixpanelAPI();

        try {
            mixpanel.deliver(delivery);
        } catch (IOException e) {
            log.info("Failed to deliver event to Mixpanel: {}", e.getMessage());
        }
    }

}
