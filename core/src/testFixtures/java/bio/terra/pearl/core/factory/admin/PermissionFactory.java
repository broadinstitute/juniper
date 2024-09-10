package bio.terra.pearl.core.factory.admin;

import bio.terra.pearl.core.model.admin.Permission;
import bio.terra.pearl.core.service.admin.PermissionService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PermissionFactory {

    @Autowired
    PermissionService permissionService;

    private Permission.PermissionBuilder<?, ?> builder(String permName) {
        return Permission.builder()
                .description(permName + " " + RandomStringUtils.randomAlphabetic(4))
                .name(permName);
    }

    public Permission buildPersisted(String permName) {
        return permissionService.create(builder(permName).build());
    }
}
