package bio.terra.pearl.core.dao;

import bio.terra.pearl.core.model.AdminUser;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class AdminUserDao extends BaseJdbiDao<AdminUser> {

    public AdminUserDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<AdminUser> getClazz() {
        return AdminUser.class;
    }
}
