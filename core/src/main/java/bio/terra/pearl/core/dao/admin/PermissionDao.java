package bio.terra.pearl.core.dao.admin;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.admin.Permission;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class PermissionDao extends BaseJdbiDao<Permission> {

    public PermissionDao(Jdbi jdbi) { super(jdbi); }

    @Override
    protected Class<Permission> getClazz() { return Permission.class; }
}
