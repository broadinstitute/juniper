package bio.terra.pearl.api.admin.service.admin;

import static org.mockito.Mockito.when;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.workflow.AdminTask;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.workflow.AdminTaskService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@ContextConfiguration(classes = AdminTaskExtService.class)
@WebMvcTest
public class AdminTaskExtServiceTests {
  @Autowired private MockMvc mockMvc;
  @Autowired private AdminTaskExtService adminTaskExtService;
  @MockBean private AuthUtilService mockAuthUtilService;
  @MockBean private AdminTaskService adminTaskService;
  @MockBean private StudyEnvironmentService studyEnvironmentService;

  @Test
  public void testGetByStudyAuthsToStudy() {
    AdminUser user = AdminUser.builder().superuser(false).build();
    when(mockAuthUtilService.authUserToStudy(user, "foo", "bar"))
        .thenThrow(new PermissionDeniedException("test1"));
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () ->
            adminTaskExtService.getByStudyEnvironment(
                "foo", "bar", EnvironmentName.irb, List.of(), user));
  }

  @Test
  public void testGetByEnrolleeAuthsToEnrollee() {
    AdminUser user = AdminUser.builder().superuser(false).build();
    when(mockAuthUtilService.authAdminUserToEnrollee(user, "code12"))
        .thenThrow(new PermissionDeniedException("test1"));
    Assertions.assertThrows(
        PermissionDeniedException.class, () -> adminTaskExtService.getByEnrollee("code12", user));
  }

  @Test
  public void testUpdateAuthsToStudy() {
    AdminUser user = AdminUser.builder().superuser(false).build();
    when(mockAuthUtilService.authUserToStudy(user, "foo", "bar"))
        .thenThrow(new PermissionDeniedException("test1"));
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () ->
            adminTaskExtService.update(
                "foo", "bar", EnvironmentName.irb, UUID.randomUUID(), new AdminTask(), user));
  }
}
