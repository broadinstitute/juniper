package bio.terra.pearl.core.service.admin;

import bio.terra.pearl.core.dao.admin.RolePermissionDao;
import bio.terra.pearl.core.model.admin.RolePermission;
import bio.terra.pearl.core.service.ImmutableEntityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RolePermissionService extends ImmutableEntityService<RolePermission, RolePermissionDao> {

    public RolePermissionService(RolePermissionDao rolePermissionDao) {
        super(rolePermissionDao);
    }

    public List<RolePermission> findAllByRoleIds(List<UUID> roleIds) {
        return dao.findAllByRoleIds(roleIds);
    }

    @Transactional
    public void deleteByRoleId(UUID roleId) {
        dao.deleteByRoleId(roleId);
    }
}
