package bio.terra.pearl.api.admin.service.kit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

public class KitExtServiceTests extends BaseSpringBootTest {
  @Autowired private KitExtService kitExtService;

  @Test
  @Transactional
  public void testGetKitRequestsForStudyEnvironmentRequiresAdmin() {
    when(mockAuthUtilService.authUserToStudy(any(), any(), any()))
        .thenThrow(new PermissionDeniedException(""));
    AdminUser adminUser = new AdminUser();
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () ->
            kitExtService.getKitRequestsForStudyEnvironment(
                adminUser, "someportal", "somestudy", EnvironmentName.sandbox));
  }

  @MockBean private AuthUtilService mockAuthUtilService;
}
