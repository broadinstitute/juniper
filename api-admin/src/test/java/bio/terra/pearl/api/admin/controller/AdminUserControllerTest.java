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

import bio.terra.pearl.api.admin.model.AdminUserDto;
import bio.terra.pearl.api.admin.model.RoleList;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.admin.AdminUserService;
import bio.terra.pearl.core.service.admin.PortalAdminUserRoleService;
import bio.terra.pearl.core.service.exception.RoleNotFoundException;
import bio.terra.pearl.core.service.exception.UserNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Together with the code generated from openapi.yml, a Controller is responsible for: *
 * implementing *Api methods * calling *Service methods * translating Service results (including
 * exceptions) into HTTP responses
 */
@ContextConfiguration(classes = AdminUserController.class)
@WebMvcTest
public class AdminUserControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private AdminUserService mockAdminUserService;

  @MockBean private PortalAdminUserRoleService mockPortalAdminUserRoleService;

  @Autowired private AdminUserController adminUserController;

  /*
   * AdminUserController.get
   */
  public static final String getEndpoint = "/api/adminUser/v1/%s";

  @Test
  public void testGetReturnsBadRequestWhenIdIsNotUuid() throws Exception {
    var request = get(getEndpoint.formatted("not-a-uuid"));

    ResultActions result = mockMvc.perform(request);

    result.andExpect(status().isBadRequest());
  }

  @Test
  public void testGetReturnsEmptyOptionalWhenAdminUserDoesNotExist() {
    when(mockAdminUserService.getAdminUser(any())).thenReturn(Optional.empty());
    UUID uuid = UUID.randomUUID();

    var result = adminUserController.get(uuid);

    assertThat(result, equalTo(ResponseEntity.notFound().build()));
    verify(mockAdminUserService).getAdminUser(uuid);
  }

  @Test
  public void testGetReturnsFoundAdminUser() {
    var userId = UUID.randomUUID();
    var adminUser = AdminUser.builder().id(userId).username("tester").build();
    var expectedAdminUserDto = new AdminUserDto().id(userId).username("tester");
    when(mockAdminUserService.getAdminUser(userId)).thenReturn(Optional.of(adminUser));

    var response = adminUserController.get(userId);

    assertThat(response, equalTo(ResponseEntity.ok(expectedAdminUserDto)));
  }

  /*
   * AdminUserController.setRoles
   */
  public static final String setRolesEndpoint = "/api/adminUser/v1/%s/roles";

  @Test
  public void testSetRolesReturnsNoContentForGoodRequest() {
    var userId = UUID.randomUUID();
    var roleNames = List.of("one", "two");

    var response = adminUserController.setRoles(userId, new RoleList().roles(roleNames));

    assertThat(response, equalTo(ResponseEntity.noContent().build()));
    verify(mockPortalAdminUserRoleService).setRoles(userId, roleNames);
  }

  @Test
  public void oldTestSetRolesReturnsBadRequestForNonExistentRole() throws Exception {
    doThrow(new RoleNotFoundException("nil"))
        .when(mockPortalAdminUserRoleService)
        .setRoles(any(), anyList());

    RoleList requestBody = new RoleList().roles(List.of("nil"));
    MockHttpServletRequestBuilder request =
        post(setRolesEndpoint.formatted(UUID.randomUUID()))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestBody));
    ResultActions result = mockMvc.perform(request);

    result.andExpect(status().isBadRequest());
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
    var userId = UUID.randomUUID();
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
