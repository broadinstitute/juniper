package bio.terra.pearl.core.dao.admin;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.admin.AdminUserWithPermissions;
import java.util.*;
import lombok.Getter;
import lombok.Setter;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.springframework.stereotype.Component;

@Component
public class AdminUserDao extends BaseMutableJdbiDao<AdminUser> {

    public AdminUserDao(Jdbi jdbi) {
        super(jdbi);
        jdbi.registerRowMapper(PortalPermission.class, BeanMapper.of(PortalPermission.class));
    }

    @Override
    protected Class<AdminUser> getClazz() {
        return AdminUser.class;
    }

    public Optional<AdminUser> findByUsername(String username) {
        return findByProperty("username", username);
    }

    public Optional<AdminUserWithPermissions> findByUsernameWithPermissions(String username) {
        Optional<AdminUser> userOpt = findByProperty("username", username);
        if (userOpt.isPresent()) {
            return loadWithPermissions(userOpt.get());
        }
        return Optional.empty();
    }

    public Optional<AdminUser> findByToken(String token) {
        return findByProperty("token", token);
    }

    public Optional<AdminUserWithPermissions> findByTokenWithPermissions(String token) {
        Optional<AdminUser> userOpt = findByProperty("token", token);
        if (userOpt.isPresent()) {
            return loadWithPermissions(userOpt.get());
        }
        return Optional.empty();
    }

    public Optional<AdminUserWithPermissions> loadWithPermissions(AdminUser user) {
        List<PortalPermission> portalPermissions = jdbi.withHandle(handle ->
                handle.createQuery("select permission.name as permissionName, portal_admin_user.portal_id as portalId" +
                                " from permission " +
                                " join role_permission on permission_id = permission.id " +
                                " join portal_admin_user_role on role_permission.role_id = portal_admin_user_role.role_id" +
                                " join portal_admin_user on portal_admin_user.id = portal_admin_user_role.portal_admin_user_id" +
                                " where portal_admin_user.admin_user_id = :adminUserId")
                        .bind("adminUserId", user.getId())
                        .mapTo(PortalPermission.class)
                        .list()
        );
        // transform the list of UUIDs and permissions into a map
        var permissionMap = new HashMap<UUID, List<String>>();
        portalPermissions.stream().forEach(portalPerm -> {
            List<String> perms = permissionMap.get(portalPerm.portalId);
            if (perms == null) {
                perms = new ArrayList<>();
            }
            perms.add(portalPerm.permissionName);
        });
        return Optional.of(new AdminUserWithPermissions(user, permissionMap));
    }

    @Getter @Setter
    public static class PortalPermission {
        private String permissionName;
        private UUID portalId;
    }

}
