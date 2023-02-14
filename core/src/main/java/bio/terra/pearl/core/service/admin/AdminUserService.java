package bio.terra.pearl.core.service.admin;

import bio.terra.pearl.core.dao.admin.AdminUserDao;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.CrudService;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        AdminUser savedUser = dao.create(adminUser);
        logger.info("Created AdminUser - id: {}, username: {}", adminUser.getId(), adminUser.getUsername());
        return savedUser;
    }

    @Override
    public Optional<AdminUser> find(UUID id) {
        return dao.find(id);
    }
}
