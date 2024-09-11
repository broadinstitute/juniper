package bio.terra.pearl.api.admin.service.admin;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.core.factory.admin.PermissionFactory;
import bio.terra.pearl.core.model.admin.Permission;
import bio.terra.pearl.core.service.admin.PermissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class PermissionServiceTests extends BaseSpringBootTest {

  @Autowired private PermissionService permissionService;
  @Autowired private PermissionFactory permissionFactory;

  @Test
  @Transactional
  public void updatePermissionDescription(TestInfo testInfo) {
    Permission initialPermission = permissionFactory.buildPersisted(getTestName(testInfo));
    permissionService.find(initialPermission.getId());

    initialPermission.setDescription("Allows the user to delete a study");
    permissionService.update(initialPermission);
    Permission updated = permissionService.find(initialPermission.getId()).get();

    assertThat(updated.getDescription(), equalTo("Allows the user to delete a study"));
  }
}
