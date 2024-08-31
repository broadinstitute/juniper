package bio.terra.pearl.core.factory.admin;

import bio.terra.pearl.core.model.admin.Role;
import bio.terra.pearl.core.service.admin.RoleService;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RoleFactory {

    @Autowired
    private RoleService roleService;

    private Role.RoleBuilder builder(String roleName) {
        return Role.builder().name(roleName).description(roleName + "_" + RandomStringUtils.randomAlphabetic(4));
    }

    public Role buildPersisted(String roleName) {
        Role role = builder(roleName).build();
        return roleService.create(role);
    }

    public Role buildPersisted(String roleName, List<String> permissions) {
        Role role = builder(roleName).build();
        return roleService.create(role, permissions);
    }
}
