package bio.terra.pearl.api.admin.service.kit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import bio.terra.pearl.api.admin.AuthAnnotationSpec;
import bio.terra.pearl.api.admin.AuthTestUtils;
import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.kit.KitOriginType;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.kit.KitRequestService;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

public class KitExtServiceTests extends BaseSpringBootTest {
  @Autowired private KitExtService kitExtService;

  @Test
  public void assertAllMethods() {
    AuthTestUtils.assertAllMethodsAnnotated(
        kitExtService,
        Map.of(
            "getKitRequestsByStudyEnvironment",
                AuthAnnotationSpec.withPortalStudyEnvPerm(AuthUtilService.BASE_PERMISSON),
            "requestKit", AuthAnnotationSpec.withPortalEnrolleePerm(AuthUtilService.BASE_PERMISSON),
            "requestKits",
                AuthAnnotationSpec.withPortalStudyEnvPerm(AuthUtilService.BASE_PERMISSON),
            "collectKit", AuthAnnotationSpec.withPortalEnrolleePerm(AuthUtilService.BASE_PERMISSON),
            "getKitRequests",
                AuthAnnotationSpec.withPortalEnrolleePerm(AuthUtilService.BASE_PERMISSON),
            "refreshKitStatuses",
                AuthAnnotationSpec.withPortalStudyEnvPerm(AuthUtilService.BASE_PERMISSON)));
  }

  @Test
  @Transactional
  public void testRequestKitsRequiresAdmin() {
    when(mockAuthUtilService.authUserToStudy(any(), any(), any()))
        .thenThrow(new PermissionDeniedException(""));
    AdminUser adminUser = new AdminUser();
    assertThrows(
        PermissionDeniedException.class,
        () ->
            kitExtService.requestKits(
                PortalStudyEnvAuthContext.of(
                    adminUser, "someportal", "somestudy", EnvironmentName.sandbox),
                Arrays.asList("enrollee1", "enrollee2"),
                new KitRequestService.KitRequestCreationDto(
                    "SALIVA", KitOriginType.SHIPPED, null, false)));
  }

  @Test
  @Transactional
  public void testGetKitRequestsForStudyEnvironmentRequiresAdmin() {
    when(mockAuthUtilService.authUserToStudy(any(), any(), any()))
        .thenThrow(new PermissionDeniedException(""));
    AdminUser adminUser = new AdminUser();
    assertThrows(
        PermissionDeniedException.class,
        () ->
            kitExtService.getKitRequestsByStudyEnvironment(
                PortalStudyEnvAuthContext.of(
                    adminUser, "someportal", "somestudy", EnvironmentName.sandbox)));
  }

  @Transactional
  @Test
  public void testRefreshKitStatusesAuthsStudy() {
    when(mockAuthUtilService.authUserToStudy(any(), any(), any()))
        .thenThrow(new PermissionDeniedException(""));
    AdminUser adminUser = new AdminUser();
    assertThrows(
        PermissionDeniedException.class,
        () ->
            kitExtService.refreshKitStatuses(
                PortalStudyEnvAuthContext.of(
                    adminUser, "someportal", "somestudy", EnvironmentName.irb)));
  }

  @MockBean private AuthUtilService mockAuthUtilService;
}
