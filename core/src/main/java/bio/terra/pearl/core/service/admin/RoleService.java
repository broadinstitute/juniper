package bio.terra.pearl.core.service.admin;

import bio.terra.pearl.core.dao.admin.RoleDao;
import bio.terra.pearl.core.model.admin.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class RoleService {

    private RoleDao roleDao;

    public RoleService(RoleDao roleDao) {
        this.roleDao = roleDao;
    }

    @Transactional
    public Role create(Role role) {
        return roleDao.create(role);
    }

    public Optional<Role> findByName(String roleName) {
        return roleDao.findByName(roleName);
    }
}
