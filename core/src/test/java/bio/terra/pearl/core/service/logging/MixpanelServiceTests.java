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

    @Test
    public void testFilterEventDataMatchesNoPatterns() {
        String originalData = """
            [
                {
                    "event": "$mp_web_page_view",
                    "properties": {
                        "$current_url": "https://sandbox.demo.localhost:3001/?referrer=google.com&language=en&timestamp=1112223333",
                        "current_url_search": "?referrer=google.com&language=en"
                    }
                }
            ]
            """;

        String expectedData = """
            [
                {
                    "event": "$mp_web_page_view",
                    "properties": {
                        "$current_url": "https://sandbox.demo.localhost:3001/?referrer=google.com&language=en&timestamp=1112223333",
                        "current_url_search": "?referrer=google.com&language=en"
                    }
                }
            ]
            """;

        String result = mixpanelService.filterEventData(originalData);
        assertEquals(expectedData, result);
    }

    @Test
    public void testFilterEventDataMatchesEmailPattern() {
        String originalData = """
            [
                {
                    "event": "$mp_web_page_view",
                    "properties": {
                        "$current_url": "https://sandbox.demo.localhost:3001/?email=jsalk@test.com&language=en",
                        "current_url_search": "?email=jsalk@test.com&language=en"
                    }
                }
            ]
            """;

        String expectedData = """
            [
                {
                    "event": "$mp_web_page_view",
                    "properties": {
                        "$current_url": "https://sandbox.demo.localhost:3001/?email={REDACTED_EMAIL}&language=en",
                        "current_url_search": "?email={REDACTED_EMAIL}&language=en"
                    }
                }
            ]
            """;

        String result = mixpanelService.filterEventData(originalData);
        assertEquals(expectedData, result);
    }

    @Test
    public void testFilterEventDataMatchesPhonePattern() {
        String originalData = """
            [
                {
                    "event": "$mp_web_page_view",
                    "properties": {
                        "$current_url": "https://sandbox.demo.localhost:3001/?phoneNumber=555-555-1234&secondPhoneNumber=(111)-111-1111&language=en",
                        "current_url_search": "?phoneNumber=555-555-1234&secondPhoneNumber=(111)-111-1111&language=en"
                    }
                }
            ]
            """;

        String expectedData = """
            [
                {
                    "event": "$mp_web_page_view",
                    "properties": {
                        "$current_url": "https://sandbox.demo.localhost:3001/?phoneNumber={REDACTED_PHONE_NUMBER}&secondPhoneNumber={REDACTED_PHONE_NUMBER}&language=en",
                        "current_url_search": "?phoneNumber={REDACTED_PHONE_NUMBER}&secondPhoneNumber={REDACTED_PHONE_NUMBER}&language=en"
                    }
                }
            ]
            """;

        String result = mixpanelService.filterEventData(originalData);
        assertEquals(expectedData, result);
    }

    @Test
    public void testFilterEventDataMatchesMultiplePatterns() {
        String originalData = """
            [
                {
                    "event": "$mp_web_page_view",
                    "properties": {
                        "$current_url": "https://sandbox.demo.localhost:3001/?email=jsalk@test.com&phoneNumber=111-222-3333",
                        "current_url_search": "?email=jsalk@test.com&phoneNumber=111-222-3333"
                    }
                }
            ]
            """;

        String expectedData = """
            [
                {
                    "event": "$mp_web_page_view",
                    "properties": {
                        "$current_url": "https://sandbox.demo.localhost:3001/?email={REDACTED_EMAIL}&phoneNumber={REDACTED_PHONE_NUMBER}",
                        "current_url_search": "?email={REDACTED_EMAIL}&phoneNumber={REDACTED_PHONE_NUMBER}"
                    }
                }
            ]
            """;
        String result = mixpanelService.filterEventData(originalData);
        assertEquals(expectedData, result);
    }



}
