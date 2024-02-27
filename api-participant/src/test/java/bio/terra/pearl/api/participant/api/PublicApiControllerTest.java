package bio.terra.pearl.api.participant.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bio.terra.pearl.api.participant.config.B2CConfigurationService;
import bio.terra.pearl.api.participant.config.VersionConfiguration;
import bio.terra.pearl.api.participant.controller.PublicApiController;
import bio.terra.pearl.api.participant.model.SystemStatus;
import bio.terra.pearl.api.participant.service.StatusService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.site.SiteMediaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@ContextConfiguration(classes = PublicApiController.class)
@WebMvcTest
class PublicApiControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private B2CConfigurationService b2CConfigurationService;
  @MockBean private SiteMediaService siteMediaService;
  @MockBean private PortalService portalService;

  @MockBean private StatusService statusService;

  @MockBean private VersionConfiguration versionConfiguration;

  @Test
  void testStatus() throws Exception {
    SystemStatus systemStatus = new SystemStatus().ok(true);
    when(statusService.getCurrentStatus()).thenReturn(systemStatus);
    this.mockMvc.perform(get("/status")).andExpect(status().isOk());
  }

  @Test
  void testStatusCheckFails() throws Exception {
    SystemStatus systemStatus = new SystemStatus().ok(false);
    when(statusService.getCurrentStatus()).thenReturn(systemStatus);
    this.mockMvc.perform(get("/status")).andExpect(status().is5xxServerError());
  }

  @Test
  void testVersion() throws Exception {
    String gitTag = "0.1.0";
    String gitHash = "abc1234";
    String github = "https://github.com/DataBiosphere/terra-java-project-template/tree/0.9.0";
    String build = "0.1.0";

    when(versionConfiguration.gitTag()).thenReturn(gitTag);
    when(versionConfiguration.gitHash()).thenReturn(gitHash);
    when(versionConfiguration.github()).thenReturn(github);
    when(versionConfiguration.build()).thenReturn(build);

    this.mockMvc
        .perform(get("/version"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.gitTag").value(gitTag))
        .andExpect(jsonPath("$.gitHash").value(gitHash))
        .andExpect(jsonPath("$.github").value(github))
        .andExpect(jsonPath("$.build").value(build));
  }

  @Test
  void testGetSwagger() throws Exception {
    this.mockMvc.perform(get("/swagger-ui.html")).andExpect(status().isOk());
  }

  @Test
  void testForwarding() throws Exception {
    // Check that all non-resource, non-api paths are forwarded to index
    this.mockMvc
        .perform(get("/ourhealth/studies/ourheart/blah/blah/blah"))
        .andExpect(forwardedUrl("/"));
    this.mockMvc.perform(get("/ourhealth/studies/ourheart")).andExpect(forwardedUrl("/"));
    this.mockMvc.perform(get("/ourhealth/ourheart")).andExpect(forwardedUrl("/"));
    this.mockMvc.perform(get("/hearthive")).andExpect(forwardedUrl("/"));
  }

  @Test
  void testHandlesMismatchedJSFingerprint() throws Exception {
    this.mockMvc
        .perform(get("/static/js/main.12345678.js"))
        .andExpect(forwardedUrl("/static/js/main.js"));
  }

  @Test
  void testHandlesMismatchedCSSFingerprint() throws Exception {
    this.mockMvc
        .perform(get("/static/css/main.12345678.css"))
        .andExpect(forwardedUrl("/static/css/main.css"));
  }

  @Test
  void testHandlesMismatchedJsChunkFingerprint() throws Exception {
    this.mockMvc
        .perform(get("/static/js/111.12345678.chunk.js"))
        .andExpect(forwardedUrl("/static/js/111.chunk.js"));
  }

  @Test
  void testResourceGets() throws Exception {
    // confirm image paths are not forwarded to index
    this.mockMvc.perform(get("/foo/bar/image.png")).andExpect(status().isNotFound());
  }
}
