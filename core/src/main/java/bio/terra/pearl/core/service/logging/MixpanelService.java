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

@Service
@Slf4j
public class MixpanelService {
    private final Environment env;

    public MixpanelService(Environment env) {
        this.env = env;
    }

    public void logEvent(String data) {
        if(!Boolean.parseBoolean(env.getProperty("env.mixpanel.enabled"))) {
            return;
        }
        
        //Mixpanel sends event data as urlencoded form data, so we need to parse the event data as a JSON array
        JSONArray events = new JSONArray(data);

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
