package bio.terra.pearl.core.dao.admin;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.admin.AdminUser;
import java.util.Optional;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class AdminUserDao extends BaseMutableJdbiDao<AdminUser> {

    public AdminUserDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<AdminUser> getClazz() {
        return AdminUser.class;
    }

    public Optional<AdminUser> findByUsername(String email) {
        return findByProperty("username", email);
    }

    public Optional<AdminUser> findByToken(String token) {
        return findByProperty("token", token);
    }
}
