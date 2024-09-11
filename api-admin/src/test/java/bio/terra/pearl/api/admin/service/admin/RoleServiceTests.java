package bio.terra.pearl.api.admin.service.admin;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.core.factory.admin.RoleFactory;
import bio.terra.pearl.core.model.admin.Role;
import bio.terra.pearl.core.service.admin.RoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class RoleServiceTests extends BaseSpringBootTest {

  @Autowired private RoleService roleService;
  @Autowired private RoleFactory roleFactory;

  @Test
  @Transactional
  public void updateRoleDescription(TestInfo testInfo) {
    Role initialRole = roleFactory.buildPersisted(getTestName(testInfo));
    roleService.find(initialRole.getId());

    initialRole.setDescription("Study staff role");
    Role updated = roleService.update(initialRole);
    roleService.find(updated.getId());

    assertThat(updated.getDescription(), equalTo("Study staff role"));
  }
}
