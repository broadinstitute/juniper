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

        JSONArray events = new JSONArray(data);

        ClientDelivery delivery = new ClientDelivery();
        MessageBuilder messageBuilder = new MessageBuilder(env.getProperty("mixpanel.token"));

        for (int i = 0; i < events.length(); i++) {
            JSONObject event = messageBuilder.event(null,
                    events.getJSONObject(i).getString("event"),
                    events.getJSONObject(i).getJSONObject("properties")
                            .put("token", env.getProperty("mixpanel.token")));
            log.info("Preparing Mixpanel event: {}", event.toString());

            delivery.addMessage(event);
        }

        MixpanelAPI mixpanel = new MixpanelAPI();

        try {
            mixpanel.deliver(delivery);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
