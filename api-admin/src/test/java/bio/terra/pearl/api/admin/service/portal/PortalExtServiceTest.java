package bio.terra.pearl.api.admin.service.portal;

import static org.mockito.Mockito.when;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.service.admin.PortalAdminUserService;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.portal.PortalEnvironmentConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.PortalService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@ContextConfiguration(classes = PortalExtService.class)
@WebMvcTest
public class PortalExtServiceTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private PortalExtService portalExtService;
  @MockBean private AuthUtilService mockAuthUtilService;
  @MockBean private PortalService mockPortalService;
  @MockBean private PortalEnvironmentService mockPortalEnvironmentService;
  @MockBean private PortalEnvironmentConfigService portalEnvironmentConfigService;
  @MockBean private PortalAdminUserService portalAdminUserService;

  @Test
  public void updateConfigHostnameRequiresSuperuser() {
    AdminUser user = AdminUser.builder().superuser(false).build();
    PortalEnvironment portalEnv =
        PortalEnvironment.builder().portalEnvironmentConfigId(UUID.randomUUID()).build();
    when(mockPortalEnvironmentService.findOne("foo", EnvironmentName.irb))
        .thenReturn(Optional.of(portalEnv));
    when(portalEnvironmentConfigService.find(portalEnv.getPortalEnvironmentConfigId()))
        .thenReturn(
            Optional.of(PortalEnvironmentConfig.builder().participantHostname("secure").build()));
    PortalEnvironmentConfig newConfig =
        PortalEnvironmentConfig.builder().participantHostname("somethingElse").build();
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () -> portalExtService.updateConfig("foo", EnvironmentName.live, newConfig, user));
  }

  @Test
  public void updateEnvRequiresSandbox() {
    AdminUser user = AdminUser.builder().superuser(true).build();
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> portalExtService.updateEnvironment("foo", EnvironmentName.irb, null, user));
  }

  @Test
  public void removePortalUserRequiresPortalAuth() {
    AdminUser operator = AdminUser.builder().superuser(false).build();
    when(mockAuthUtilService.authUserToPortal(operator, "testPortal"))
        .thenThrow(new PermissionDeniedException("test"));
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () -> portalExtService.removeUserFromPortal(UUID.randomUUID(), "testPortal", operator));
  }
}
