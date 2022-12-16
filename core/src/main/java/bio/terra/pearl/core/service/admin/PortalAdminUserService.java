package bio.terra.pearl.core.service.admin;

import bio.terra.pearl.core.dao.admin.PortalAdminUserDao;
import bio.terra.pearl.core.model.admin.PortalAdminUser;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class PortalAdminUserService {

    private PortalAdminUserDao portalAdminUserDao;

    public PortalAdminUserService(PortalAdminUserDao portalAdminUserDao) {
        this.portalAdminUserDao = portalAdminUserDao;
    }

    public PortalAdminUser create(PortalAdminUser portalAdminUser) {
        return portalAdminUserDao.create(portalAdminUser);
    }

    public Optional<PortalAdminUser> findOne(UUID portalAdminUserId) {
        return portalAdminUserDao.find(portalAdminUserId);
    }
}
