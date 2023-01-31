package bio.terra.pearl.core.service.admin;

import bio.terra.pearl.core.dao.admin.RolePermissionDao;
import bio.terra.pearl.core.model.admin.RolePermission;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RolePermissionService extends CrudService<RolePermission, RolePermissionDao> {

    public RolePermissionService(RolePermissionDao rolePermissionDao) {
        super(rolePermissionDao);
    }

    public List<RolePermission> findByRole(UUID roleId) {
        return dao.findByRole(roleId);
    }
}
