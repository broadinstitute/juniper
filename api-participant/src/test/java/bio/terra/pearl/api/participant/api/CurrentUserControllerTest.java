package bio.terra.pearl.api.participant.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bio.terra.pearl.api.participant.controller.CurrentUserController;
import bio.terra.pearl.api.participant.service.CurrentUserService;
import bio.terra.pearl.api.participant.service.RequestUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@ContextConfiguration(classes = CurrentUserController.class)
@WebMvcTest
class CurrentUserControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private RequestUtilService requestUtilService;
  @MockBean private CurrentUserService currentUserService;

  @Test
  void testEnrolleeSerialization() throws Exception {
    String token = "faketoken";

    when(requestUtilService.requireToken(any())).thenReturn(token);

    when(currentUserService.refresh(token, "testportal", EnvironmentName.sandbox))
        .thenReturn(
            new CurrentUserService.UserLoginDto(
                ParticipantUser.builder().build(),
                Profile.builder().build(),
                List.of(),
                List.of(Enrollee.builder().shortcode("OHSALK").researchId("12SALK").build()),
                List.of()));

    this.mockMvc
        .perform(post("/api/portals/v1/testportal/env/sandbox/current-user/refresh"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.enrollees[0].shortcode").value("OHSALK"))
        .andExpect(jsonPath("$.enrollees[0].researchId").doesNotExist());
  }
}
