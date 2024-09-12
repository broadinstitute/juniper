package bio.terra.pearl.core.dao.admin;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.admin.AdminDataChange;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AdminDataChangeDao extends BaseJdbiDao<AdminDataChange> {
    public AdminDataChangeDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<AdminDataChange> getClazz() {
        return AdminDataChange.class;
    }

    public void deleteByPortalId(UUID portalId) {
        deleteByProperty("portal_id", portalId);
    }

    public void deleteByResponsibleAdminUserId(UUID adminUserId) {
        deleteByProperty("responsible_admin_user_id", adminUserId);
    }
}
