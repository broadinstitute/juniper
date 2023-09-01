package bio.terra.pearl.api.admin.service.portal;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.portal.PortalEnvironmentConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.PortalService;
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
  @MockBean private PortalEnvironmentService portalEnvironmentService;
  @MockBean private PortalEnvironmentConfigService portalEnvironmentConfigService;

  @Test
  public void updateConfigRequiresSuperuser() {
    AdminUser user = AdminUser.builder().superuser(false).build();
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () -> portalExtService.updateConfig("foo", EnvironmentName.live, null, user));
  }

  @Test
  public void updateEnvRequiresSandbox() {
    AdminUser user = AdminUser.builder().superuser(true).build();
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> portalExtService.updateEnvironment("foo", EnvironmentName.irb, null, user));
  }
}
