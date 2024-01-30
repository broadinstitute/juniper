package bio.terra.pearl.api.admin.controller;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bio.terra.pearl.api.admin.controller.admin.AdminUserController;
import bio.terra.pearl.api.admin.model.AdminUserDto;
import bio.terra.pearl.api.admin.model.RoleList;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.admin.AdminUserExtService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.admin.PortalAdminUserRoleService;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.exception.RoleNotFoundException;
import bio.terra.pearl.core.service.exception.UserNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
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
  public static final String getEndpoint = "/api/adminUser/v1/%s";

  @Test
  public void testGetReturnsBadRequestWhenIdIsNotUuid() throws Exception {
      MockHttpServletRequestBuilder request = get(getEndpoint.formatted("not-a-uuid"));

    ResultActions result = mockMvc.perform(request);

    result.andExpect(status().isBadRequest());
  }

  @Test
  public void testGetReturnsEmptyOptionalWhenAdminUserDoesNotExist() {
    when(adminUserExtService.get(any(), any())).thenReturn(Optional.empty());
    UUID uuid = UUID.randomUUID();

      ResponseEntity<AdminUserDto> result = adminUserController.get(uuid);

    assertThat(result, equalTo(ResponseEntity.notFound().build()));
    verify(adminUserExtService).get(uuid, null);
  }

  @Test
  public void testGetReturnsFoundAdminUser() {
      UUID userId = UUID.randomUUID();
      AdminUser adminUser = AdminUser.builder().id(userId).username("tester").build();
      AdminUserDto expectedAdminUserDto = new AdminUserDto().id(userId).superuser(false).username("tester");
    when(adminUserExtService.get(userId, null)).thenReturn(Optional.of(adminUser));
      ResponseEntity<AdminUserDto> response = adminUserController.get(userId);

    assertThat(response, equalTo(ResponseEntity.ok(expectedAdminUserDto)));
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

  /*
   * AdminUserController.setRoles
   */
  public static final String setRolesEndpoint = "/api/adminUser/v1/%s/roles";

  @Test
  public void testSetRolesReturnsNoContentForGoodRequest() {
      UUID userId = UUID.randomUUID();
      List<String> roleNames = List.of("one", "two");
    when(mockPortalAdminUserRoleService.setRoles(userId, roleNames)).thenReturn(roleNames);
      RoleList expectedRoleList = new RoleList().roles(roleNames);

      ResponseEntity<RoleList> response = adminUserController.setRoles(userId, new RoleList().roles(roleNames));

    assertThat(response, equalTo(ResponseEntity.ok(expectedRoleList)));
    verify(mockPortalAdminUserRoleService).setRoles(userId, roleNames);
  }

  @Test
  public void testSetRolesThrowsForNonExistentRole() {
    doThrow(new RoleNotFoundException("nil"))
        .when(mockPortalAdminUserRoleService)
        .setRoles(any(), anyList());

    Executable action =
        () -> adminUserController.setRoles(UUID.randomUUID(), new RoleList().roles(List.of("nil")));

    assertThrows(RoleNotFoundException.class, action);
  }

  /**
   * Verify that a `ValidationException` gets translated into a bad request response. This test
   * happens to use the endpoint for setting roles because that endpoint can easily be invoked in a
   * way that results in a validation exception. However, this is not intended as a test of that
   * specific endpoint.
   *
   * @throws Exception from mockMvc
   */
  @Test
  public void testValidationExceptionReturnsBadRequest() throws Exception {
      UUID userId = UUID.randomUUID();
    doThrow(new UserNotFoundException(userId))
        .when(mockPortalAdminUserRoleService)
        .setRoles(any(), anyList());

    MockHttpServletRequestBuilder request =
        post(setRolesEndpoint.formatted(userId))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new RoleList()));
    ResultActions result = mockMvc.perform(request);

    result.andExpectAll(
        status().isBadRequest(), content().string(containsString(String.valueOf(userId))));
  }
}
