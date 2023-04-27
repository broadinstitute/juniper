package bio.terra.pearl.api.admin.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import bio.terra.pearl.api.admin.config.VersionConfiguration;
import bio.terra.pearl.api.admin.model.SystemStatus;
import bio.terra.pearl.api.admin.service.ConfigExtService;
import bio.terra.pearl.api.admin.service.StatusService;
import java.util.Map;
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

  @MockBean private ConfigExtService configExtService;

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
    this.mockMvc.perform(get("/ourhealth/studies/ourheart")).andExpect(forwardedUrl("/"));
    this.mockMvc.perform(get("/hearthive")).andExpect(forwardedUrl("/"));
  }

  @Test
  void testResourceGets() throws Exception {
    // confirm image paths are not forwarded to index
    this.mockMvc.perform(get("/foo/bar/image.png")).andExpect(status().isNotFound());
  }

  @Test
  void testGetConfig() throws Exception {
    when(configExtService.getConfigMap()).thenReturn(Map.of("foo", "bar"));
    this.mockMvc
        .perform(get("/config"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.foo").value("bar"));
  }
}
