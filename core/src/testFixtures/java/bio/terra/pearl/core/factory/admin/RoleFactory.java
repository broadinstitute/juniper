package bio.terra.pearl.core.factory.admin;

import bio.terra.pearl.core.model.admin.Role;
import bio.terra.pearl.core.service.admin.RoleService;
import java.util.List;
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
        Role role = builder(testName).build();
        return roleService.create(role);
    }

    public Role buildPersisted(String testName, List<String> permissions) {
        Role role = builder(testName).build();
        return roleService.create(role, permissions);
    }
}
