package bio.terra.pearl.core.dao.admin;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.admin.RolePermission;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class RolePermissionDao extends BaseJdbiDao<RolePermission> {

    public RolePermissionDao(Jdbi jdbi) {
        super(jdbi);
    }

    public List<RolePermission> findByRole(UUID roleId) {
        return findAllByProperty("role_id", roleId);
    }

    public void deleteByRoleId(UUID roleId) {
        deleteByProperty("role_id", roleId);
    }

    @Override
    protected Class<RolePermission> getClazz() { return RolePermission.class; }
}
