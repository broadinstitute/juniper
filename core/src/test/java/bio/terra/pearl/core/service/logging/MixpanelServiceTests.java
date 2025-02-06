package bio.terra.pearl.core.service.logging;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.service.portal.PortalEnvironmentConfigService;
import org.json.JSONObject;
import org.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class MixpanelServiceTests extends BaseSpringBootTest {
    @Autowired private MixpanelService mixpanelService;
    @Autowired private LoggingConfigCache loggingConfigCache;
    @Autowired private Environment env;
    @Autowired private PortalEnvironmentConfigService portalEnvironmentConfigService;
    @Autowired private PortalEnvironmentFactory portalEnvironmentFactory;

    @Test
    public void testBuildEvent() {
        JSONObject event = new JSONObject();
        event.put("event", "test_event");
        JSONObject properties = new JSONObject();
        properties.put("key", "value");
        event.put("properties", properties);

        env = mock(Environment.class);
        when(env.getProperty("env.mixpanel.token")).thenReturn("test-token");
        MixpanelService.MixpanelConfig mixpanelConfig = new MixpanelService.MixpanelConfig(env);
        MixpanelService mockedMixpanelService = new MixpanelService(mixpanelConfig, loggingConfigCache);

        JSONObject result = mockedMixpanelService.buildEvent(event, "test-token");

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
        verify(mockedMixpanelService, never()).buildEvent(any(), any());
    }

    @Test
    public void testEnableMixpanel() {
        Environment env = mock(Environment.class);
        when(env.getProperty("env.mixpanel.enabled")).thenReturn("true");
        when(env.getProperty("env.mixpanel.token")).thenReturn("test-token");
        MixpanelService.MixpanelConfig mixpanelConfig = new MixpanelService.MixpanelConfig(env);
        // Create a spy on the MixpanelService instance
        MixpanelService mixpanelService = new MixpanelService(mixpanelConfig, loggingConfigCache);
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

        verify(spyMixpanelService, times(2)).buildEvent(any(), any());
        verify(spyMixpanelService, times(1)).deliverEvents(any());
    }

    @Test
    @Transactional
    public void testSendsToCustomProject(TestInfo info) {
        Environment env = mock(Environment.class);
        when(env.getProperty("env.mixpanel.enabled")).thenReturn("true");
        when(env.getProperty("env.mixpanel.token")).thenReturn("test-token");
        MixpanelService.MixpanelConfig mixpanelConfig = new MixpanelService.MixpanelConfig(env);
        // Create a spy on the MixpanelService instance
        MixpanelService mixpanelService = new MixpanelService(mixpanelConfig, loggingConfigCache);
        MixpanelService spyMixpanelService = spy(mixpanelService);

        PortalEnvironment portalEnv =  portalEnvironmentFactory.buildPersisted(getTestName(info));
        PortalEnvironmentConfig envConfig = portalEnvironmentConfigService.find(portalEnv.getPortalEnvironmentConfigId()).get();
        envConfig.setParticipantHostname("somedomain.org");
        envConfig.setMixpanelToken("custom-token");
        portalEnvironmentConfigService.update(envConfig);
        loggingConfigCache.configCacheEvict();


        JSONObject event = new JSONObject();
        event.put("event", "test_event");
        JSONObject properties = new JSONObject();
        properties.put("key", "value");
        properties.put("$current_url", "https://somedomain.org/page");
        event.put("properties", properties);

        JSONObject event2 = new JSONObject();
        event2.put("event", "test_event2");
        JSONObject properties2 = new JSONObject();
        properties2.put("key", "value2");
        properties2.put("$current_url", "https://anotherdomain.com");
        event2.put("properties", properties2);

        JSONArray events = new JSONArray();
        events.put(event);
        events.put(event2);

        spyMixpanelService.logEvent(events.toString());
        ArgumentCaptor<JSONObject> objCaptor = ArgumentCaptor.forClass(JSONObject.class);
        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(spyMixpanelService, times(3)).buildEvent(objCaptor.capture(), tokenCaptor.capture());

        assertThat(tokenCaptor.getAllValues(), contains("test-token", "custom-token", "test-token"));
        assertThat(objCaptor.getAllValues().stream().map(obj ->
                ((JSONObject) obj.get("properties")).get("$current_url")).toList(),
                contains("https://somedomain.org/page", "https://somedomain.org/page", "https://anotherdomain.com"));
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
