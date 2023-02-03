package bio.terra.pearl.core.factory.admin;

import bio.terra.pearl.core.model.admin.Role;
import bio.terra.pearl.core.service.admin.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RoleFactory {

    @Autowired
    private RoleService roleService;

    private Role.RoleBuilder builder(String testName) {
        return Role.builder().name(testName);
    }

    public Role buildPersisted(String testName) {
        var role = builder(testName).build();
        return roleService.create(role);
    }
}
