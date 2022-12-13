package bio.terra.pearl.core.service;

import bio.terra.pearl.core.dao.AdminUserDao;
import bio.terra.pearl.core.model.AdminUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class AdminUserService extends CrudService<AdminUser, AdminUserDao> {

    public AdminUserService(AdminUserDao adminUserDao) {
        super(adminUserDao);
    }

    public Optional<AdminUser> findByUsername(String email) {
        return dao.findByUsername(email);
    }

    @Transactional
    @Override
    public AdminUser create(AdminUser adminUser) {
        return dao.create(adminUser);
    }

    @Override
    public Optional<AdminUser> find(UUID id) {
        return dao.find(id);
    }
}
