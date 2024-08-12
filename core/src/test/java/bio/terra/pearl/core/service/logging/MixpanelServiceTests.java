package bio.terra.pearl.core.service.logging;

import bio.terra.pearl.core.BaseSpringBootTest;
import org.json.JSONObject;
import org.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class MixpanelServiceTests extends BaseSpringBootTest {
    @Autowired private MixpanelService mixpanelService;
    @Autowired private Environment env;

    @Test
    public void testBuildEvent() {
        JSONObject event = new JSONObject();
        event.put("event", "test_event");
        JSONObject properties = new JSONObject();
        properties.put("key", "value");
        event.put("properties", properties);

        env = mock(Environment.class);
        when(env.getProperty("mixpanel.token")).thenReturn("test-token");
        MixpanelService mockedMixpanelService = new MixpanelService(env);

        JSONObject result = mockedMixpanelService.buildEvent(event);

        JSONObject message = result.getJSONObject("message");

        assertEquals("test_event", message.getString("event"));
        assertEquals("value", message.getJSONObject("properties").getString("key"));
        // Check that the token is added to the properties
        assertEquals("test-token", message.getJSONObject("properties").getString("token"));
    }

    @Test
    public void testDisableMixpanel() {
        //Mixpanel is disabled by default, so we'll test that it doesn't attempt to delivery any events
        MixpanelService mockedMixpanelService = mock(MixpanelService.class);

        mockedMixpanelService.logEvent("[]");

        verify(mockedMixpanelService, never()).deliverEvents(any());
        verify(mockedMixpanelService, never()).buildEvent(any());
    }

    @Test
    public void testEnableMixpanel() {
        Environment env = mock(Environment.class);
        when(env.getProperty("env.mixpanel.enabled")).thenReturn("true");
        when(env.getProperty("mixpanel.token")).thenReturn("test-token");

        // Create a spy on the MixpanelService instance
        MixpanelService mixpanelService = new MixpanelService(env);
        MixpanelService spyMixpanelService = spy(mixpanelService);

        JSONObject event = new JSONObject();
        event.put("event", "test_event");
        JSONObject properties = new JSONObject();
        properties.put("key", "value");
        event.put("properties", properties);

        JSONObject event2 = new JSONObject();
        event2.put("event", "test_event2");
        JSONObject properties2 = new JSONObject();
        properties2.put("key", "value2");
        event2.put("properties", properties2);

        JSONArray events = new JSONArray();
        events.put(event);
        events.put(event2);

        spyMixpanelService.logEvent(events.toString());

        verify(spyMixpanelService, times(2)).buildEvent(any());
        verify(spyMixpanelService, times(1)).deliverEvents(any());
    }

}
