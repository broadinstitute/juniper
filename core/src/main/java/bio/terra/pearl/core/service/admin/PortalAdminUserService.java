package bio.terra.pearl.core.service.admin;

import bio.terra.pearl.core.dao.admin.PortalAdminUserDao;
import bio.terra.pearl.core.model.admin.*;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.ImmutableEntityService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.exception.UserNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortalAdminUserService extends AdminDataAuditedService<PortalAdminUser, PortalAdminUserDao> {

    private PortalAdminUserRoleService portalAdminUserRoleService;

    public PortalAdminUserService(PortalAdminUserDao portalAdminUserDao,
                                  PortalAdminUserRoleService portalAdminUserRoleService,
                                  AdminDataChangeService adminDataChangeService,
                                  ObjectMapper objectMapper) {
        super(portalAdminUserDao, adminDataChangeService, objectMapper);
        this.portalAdminUserRoleService = portalAdminUserRoleService;
    }

    public PortalAdminUser create(PortalAdminUser portalAdminUser, DataAuditInfo auditInfo) {
        PortalAdminUser paUser = super.create(portalAdminUser, auditInfo);
        if (portalAdminUser.getPortalAdminUserRoles() != null) {
            for (PortalAdminUserRole pauRole : portalAdminUser.getPortalAdminUserRoles()) {
                pauRole.setPortalAdminUserId(paUser.getId());
                portalAdminUserRoleService.create(pauRole, auditInfo);
                paUser.getPortalAdminUserRoles().add(pauRole);
            }
        }
        return paUser;
    }

    public List<PortalAdminUser> findByPortal(UUID portalId) {
        return dao.findByPortal(portalId);
    }

    public List<PortalAdminUser> findByAdminUser(UUID adminUserId) {
        return dao.findByUserId(adminUserId);
    }

    public Optional<PortalAdminUser> findByUserIdAndPortal(UUID adminUserId, UUID portalId) {
        return dao.findByUserIdAndPortal(adminUserId, portalId);
    }

    public Optional<PortalAdminUser> findWithRolesAndPermissions(UUID portalAdminUserId) {
        Optional<PortalAdminUser> portalAdminUserOpt = dao.find(portalAdminUserId);
        return portalAdminUserOpt.map(portalAdminUser -> attachRolesAndPermissions(portalAdminUser));
    }

    public PortalAdminUser attachRolesAndPermissions(PortalAdminUser portalAdminUser) {
        List<Role> roles = portalAdminUserRoleService.findRolesWithPermissionsByPortalAdminUserId(portalAdminUser.getId());
        portalAdminUser.setRoles(roles);
        return portalAdminUser;
    }

    public boolean isUserInPortal(UUID userId, UUID portalId) {
        return dao.isUserInPortal(userId, portalId);
    }

    @Transactional
    public void deleteByUserId(UUID adminUserId, DataAuditInfo auditInfo) {
        List<PortalAdminUser> portalAdminUsers = dao.findByUserId(adminUserId);
        // for now this will probably be a small list, so it's fine to just delete them one by one
        for (PortalAdminUser portalAdminUser : portalAdminUsers) {
            delete(portalAdminUser.getId(), auditInfo, CascadeProperty.EMPTY_SET);
        }
    }

    /** this operation is not audited, as it is only done when nuking an entire portal */
    @Transactional
    public void deleteByPortalId(UUID portalId, DataAuditInfo auditInfo) {
        List<PortalAdminUser> portalAdminUsers = dao.findByPortal(portalId);
        for (PortalAdminUser portalAdminUser : portalAdminUsers) {
            delete(portalAdminUser.getId(), auditInfo, CascadeProperty.EMPTY_SET);
        }
    }

    @Transactional
    @Override
    public void delete(UUID id, DataAuditInfo auditInfo, Set<CascadeProperty> cascades) {
        PortalAdminUser existing = dao.find(id)
                .orElseThrow(() -> new NotFoundException("PortalAdminUser not found: " + id));
        auditInfo.setAdminUserId(existing.getAdminUserId());
        portalAdminUserRoleService.deleteByPortalAdminUserId(id);
        super.delete(id, auditInfo, cascades);
    }
}
