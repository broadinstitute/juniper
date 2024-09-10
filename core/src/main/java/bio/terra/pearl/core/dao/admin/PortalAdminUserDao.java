package bio.terra.pearl.core.dao.admin;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.admin.PortalAdminUser;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class PortalAdminUserDao extends BaseMutableJdbiDao<PortalAdminUser> {

    public PortalAdminUserDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<PortalAdminUser> getClazz() {
        return PortalAdminUser.class;
    }

    public boolean isUserInPortal(UUID userId, UUID portalId) {
        return findByTwoProperties("portal_id", portalId, "admin_user_id", userId).isPresent();
    }

    public List<PortalAdminUser> findByPortal(UUID portalId) {
        return findAllByProperty("portal_id", portalId);
    }

    public List<PortalAdminUser> findByUserId(UUID userId) {
        return findAllByProperty("admin_user_id", userId);
    }

    public Optional<PortalAdminUser> findByUserIdAndPortal(UUID adminUserId, UUID portalId) {
        return findByTwoProperties("admin_user_id", adminUserId,
                "portal_id", portalId);
    }

    public void deleteByUserId(UUID adminUserId) {
        deleteByProperty("admin_user_id", adminUserId);
    }

    public void deleteByPortalId(UUID portalId) {
        deleteByProperty("portal_id", portalId);
    }
}
