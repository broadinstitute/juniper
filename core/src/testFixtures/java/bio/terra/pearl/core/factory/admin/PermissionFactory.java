package bio.terra.pearl.core.factory.admin;

import bio.terra.pearl.core.model.admin.Permission;
import bio.terra.pearl.core.service.admin.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PermissionFactory {

    @Autowired
    PermissionService permissionService;

    private Permission.PermissionBuilder<?, ?> builder(String testName) { return Permission.builder().name(testName); }

    public Permission buildPersisted(String testName) {
        return permissionService.create(builder(testName).build());
    }
}
