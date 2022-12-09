package bio.terra.pearl.core.dao.admin;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.admin.Role;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.Optional;

// TODO: add tests???
@Component
public class RoleDao extends BaseJdbiDao<Role> {

    public RoleDao(Jdbi jdbi) {
        super(jdbi);
    }

    public Optional<Role> findByName(String name) {
        return findByProperty("name", name);
    }

    @Override
    protected Class<Role> getClazz() {
        return Role.class;
    }
}
