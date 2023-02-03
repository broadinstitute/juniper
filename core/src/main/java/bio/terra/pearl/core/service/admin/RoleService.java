package bio.terra.pearl.core.service.admin;

import bio.terra.pearl.core.dao.admin.RoleDao;
import bio.terra.pearl.core.model.admin.Role;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class RoleService extends CrudService<Role, RoleDao> {

    public RoleService(RoleDao roleDao) {
        super(roleDao);
    }

    @Transactional
    public Role create(Role role) {
        return dao.create(role);
    }

    public Optional<Role> findOne(UUID roleId) { return dao.find(roleId); }

    public Optional<Role> findByName(String roleName) {
        return dao.findByName(roleName);
    }
}
