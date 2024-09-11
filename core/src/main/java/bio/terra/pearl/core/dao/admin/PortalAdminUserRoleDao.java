package bio.terra.pearl.core.dao.admin;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.admin.PortalAdminUserRole;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

// TODO: add tests???
@Component
public class PortalAdminUserRoleDao extends BaseMutableJdbiDao<PortalAdminUserRole> {

    public PortalAdminUserRoleDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<PortalAdminUserRole> getClazz() {
        return PortalAdminUserRole.class;
    }

    public List<PortalAdminUserRole> findByPortalAdminUserId(UUID portalAdminUserId) {
        return findAllByProperty("portal_admin_user_id", portalAdminUserId);
    }

    public List<PortalAdminUserRole> findAllByPortalAdminUserIds(List<UUID> portalAdminUserIds) {
        return findAllByPropertyCollection("portal_admin_user_id", portalAdminUserIds);
    }

    public void deleteByPortalAdminUserId(UUID portalAdminUserId) {
        deleteByProperty("portal_admin_user_id", portalAdminUserId);
    }
}
