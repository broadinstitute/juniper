package bio.terra.pearl.api.admin.controller;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bio.terra.pearl.api.admin.controller.admin.AdminUserController;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.admin.AdminUserExtService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.admin.PortalAdminUserRoleService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@ContextConfiguration(classes = AdminUserController.class)
@WebMvcTest
public class AdminUserControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private PortalAdminUserRoleService mockPortalAdminUserRoleService;
  @MockBean private AdminUserExtService adminUserExtService;
  @MockBean private AuthUtilService authUtilService;

  @Autowired private AdminUserController adminUserController;
  @SpyBean private GlobalExceptionHandler globalExceptionHandler;

  /*
   * AdminUserController.get
   */
  public static final String getEndpoint = "/api/adminUsers/v1/%s";

  @Test
  public void testGetReturnsBadRequestWhenIdIsNotUuid() throws Exception {
    MockHttpServletRequestBuilder request = get(getEndpoint.formatted("not-a-uuid"));
    ResultActions result = mockMvc.perform(request);
    result.andExpect(status().isBadRequest());
  }

  @Test
  public void testGetReturnsNotFoundWhenAdminUserDoesNotExist() throws Exception {
    when(adminUserExtService.get(any(), any(), any()))
        .thenThrow(new NotFoundException("user not found"));
    UUID uuid = UUID.randomUUID();
    MockHttpServletRequestBuilder request = get(getEndpoint.formatted(uuid));

    ResultActions result = mockMvc.perform(request);
    result.andExpect(status().isNotFound());
  }

  @Test
  public void testGetReturnsFoundAdminUser() {
    UUID userId = UUID.randomUUID();
    AdminUser adminUser = AdminUser.builder().id(userId).username("tester").build();
    when(adminUserExtService.get(userId, null, null)).thenReturn(adminUser);
    ResponseEntity<Object> response = adminUserController.get(userId, null);

    assertThat(response, equalTo(ResponseEntity.ok(adminUser)));
  }

  @Test
  public void testGetAllErrorsIfAuthFails() {
    ResponseEntity<Object> response = adminUserController.getAll();
    when(authUtilService.requireAdminUser(any())).thenThrow(PermissionDeniedException.class);
    Assertions.assertThrows(PermissionDeniedException.class, () -> adminUserController.getAll());
  }

  @Test
  public void testGetByPortalErrorsIfAuthFails() {
    ResponseEntity<Object> response = adminUserController.getAll();
    when(authUtilService.requireAdminUser(any())).thenThrow(PermissionDeniedException.class);
    Assertions.assertThrows(
        PermissionDeniedException.class, () -> adminUserController.getByPortal("whatever"));
  }
}
