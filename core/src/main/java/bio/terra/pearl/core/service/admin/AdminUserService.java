package bio.terra.pearl.core.service.admin;

import bio.terra.pearl.core.dao.admin.AdminUserDao;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService extends CrudService<AdminUser, AdminUserDao> {
    private PortalAdminUserService portalAdminUserService;

    public AdminUserService(AdminUserDao adminUserDao, PortalAdminUserService portalAdminUserService) {
        super(adminUserDao);
        this.portalAdminUserService = portalAdminUserService;
    }

    public Optional<AdminUser> findByUsername(String email) {
        return dao.findByUsername(email);
    }

    @Transactional
    @Override
    public AdminUser create(AdminUser adminUser) {
        AdminUser savedUser = dao.create(adminUser);
        logger.info("Created AdminUser - id: {}, username: {}", savedUser.getId(), savedUser.getUsername());
        return savedUser;
    }

    @Override
    @Transactional
    public void delete(UUID adminUserId, Set<CascadeProperty> cascade) {
        portalAdminUserService.deleteByUserId(adminUserId);
        dao.delete(adminUserId);
    }

    @Override
    public Optional<AdminUser> find(UUID id) {
        return dao.find(id);
    }
}
