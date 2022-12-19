package bio.terra.pearl.core.service.admin;

import bio.terra.pearl.core.dao.admin.AdminUserDao;
import bio.terra.pearl.core.model.admin.AdminUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class AdminUserService {

    private final AdminUserDao adminUserDao;

    public AdminUserService(AdminUserDao adminUserDao) {
        this.adminUserDao = adminUserDao;
    }

    @Transactional
    public AdminUser createAdminUser(AdminUser adminUser) {
        return adminUserDao.create(adminUser);
    }

    public Optional<AdminUser> getAdminUser(UUID id) {
        return adminUserDao.find(id);
    }
}
