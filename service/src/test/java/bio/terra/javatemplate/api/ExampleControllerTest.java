package bio.terra.javatemplate.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bio.terra.common.iam.BearerToken;
import bio.terra.common.iam.BearerTokenFactory;
import bio.terra.common.iam.SamUser;
import bio.terra.common.iam.SamUserFactory;
import bio.terra.javatemplate.config.SamConfiguration;
import bio.terra.javatemplate.controller.ExampleController;
import bio.terra.javatemplate.iam.SamService;
import bio.terra.javatemplate.model.Example;
import bio.terra.javatemplate.service.ExampleService;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@ContextConfiguration(classes = ExampleController.class)
@WebMvcTest
public class ExampleControllerTest {
  @MockBean ExampleService serviceMock;
  @MockBean SamUserFactory samUserFactoryMock;
  @MockBean BearerTokenFactory bearerTokenFactory;
  @MockBean SamConfiguration samConfiguration;
  @MockBean SamService samService;

  @Autowired private MockMvc mockMvc;

  private SamUser testUser =
      new SamUser(
          "test@email",
          UUID.randomUUID().toString(),
          new BearerToken(UUID.randomUUID().toString()));

  @BeforeEach
  void beforeEach() {
    when(samUserFactoryMock.from(any(HttpServletRequest.class), any())).thenReturn(testUser);
  }

  @Test
  void testGetMessageOk() throws Exception {
    var example = new Example(testUser.getSubjectId(), "message");
    when(serviceMock.getExampleForUser(testUser.getSubjectId())).thenReturn(Optional.of(example));

    mockMvc
        .perform(get("/api/example/v1/message"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().string(example.message()));
  }

  @Test
  void testGetMessageNotFound() throws Exception {
    when(serviceMock.getExampleForUser(testUser.getSubjectId())).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/example/v1/message")).andExpect(status().isNotFound());
  }
}
