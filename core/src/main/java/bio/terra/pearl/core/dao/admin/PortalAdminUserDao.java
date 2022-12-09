package bio.terra.pearl.core.dao.admin;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.admin.PortalAdminUser;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class PortalAdminUserDao extends BaseJdbiDao<PortalAdminUser> {

    public PortalAdminUserDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<PortalAdminUser> getClazz() {
        return PortalAdminUser.class;
    }
}
