package bio.terra.pearl.api.admin.service.notifications;

import static org.mockito.Mockito.when;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

public class TriggerExtAuthServiceTests extends BaseSpringBootTest {
  @Autowired private TriggerExtService triggerExtService;

  @MockBean private AuthUtilService mockAuthUtilService;

  @Test
  public void replaceConfigAuthsToPortal() {
    AdminUser user = AdminUser.builder().superuser(false).build();
    when(mockAuthUtilService.authUserToPortal(user, "foo"))
        .thenThrow(new PermissionDeniedException("test1"));
    Assertions.assertThrows(
        NotFoundException.class,
        () ->
            triggerExtService.replace(
                PortalStudyEnvAuthContext.of(user, "foo", "studyCode", EnvironmentName.live),
                UUID.randomUUID(),
                null));
  }

  @Test
  public void findForSutdyAuthsToStudy() {
    AdminUser user = AdminUser.builder().superuser(false).build();
    when(mockAuthUtilService.authUserToStudy(user, "foo", "bar"))
        .thenThrow(new PermissionDeniedException("test1"));
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () -> triggerExtService.findForStudy(user, "foo", "bar", EnvironmentName.live));
  }
}
