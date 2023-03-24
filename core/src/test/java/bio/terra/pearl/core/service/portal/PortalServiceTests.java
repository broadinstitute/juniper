package bio.terra.pearl.core.service.portal;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.AdminUserFactory;
import bio.terra.pearl.core.factory.PortalFactory;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.admin.PortalAdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.admin.PortalAdminUserService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PortalServiceTests extends BaseSpringBootTest {
    @Autowired
    private PortalFactory portalFactory;
    @Autowired
    private AdminUserFactory adminUserFactory;
    @Autowired
    private PortalService portalService;
    @Autowired
    private PortalAdminUserService portalAdminUserService;

    @Test
    public void authAdminToPortalRejectsUsersNotInPortal() {
        AdminUser user = adminUserFactory.buildPersisted("authAdminToPortalRejectsUsersNotInPortal");
        Portal portal = portalFactory.buildPersisted("authAdminToPortalRejectsUsersNotInPortal");
        Assertions.assertThrows(PermissionDeniedException.class, () -> {
            portalService.authAdminToPortal(user, portal.getShortcode());
        });

        // now add the user to a second portal
        Portal portal2 = portalFactory.buildPersisted("authAdminToPortalRejectsUsersNotInPortal2");
        portalAdminUserService.create(PortalAdminUser.builder()
                        .adminUserId(user.getId())
                        .portalId(portal2.getId())
                        .build());
        // confirm user can access second portal
        Portal authedPortal = portalService.authAdminToPortal(user, portal2.getShortcode());
        assertThat(authedPortal.getId(), equalTo(portal2.getId()));
        assertThat(portalService.checkAdminIsInPortal(user, portal.getId()), equalTo(false));
        assertThat(portalService.checkAdminIsInPortal(user, portal2.getId()), equalTo(true));

        // but still not the first
        Assertions.assertThrows(PermissionDeniedException.class, () -> {
            portalService.authAdminToPortal(user, portal.getShortcode());
        });
    }

    @Test
    public void authAdminToPortalRejectsNotFoundPortal() {
        AdminUser user = adminUserFactory.buildPersisted("authAdminToPortalRejectsNotFoundPortal");
        Assertions.assertThrows(NotFoundException.class, () -> {
            portalService.authAdminToPortal(user, "DOES_NOT_EXIST");
        });
    }

    @Test
    public void authAdminToPortalAllowsSuperUser() {
        AdminUser user = adminUserFactory.buildPersisted(
                adminUserFactory.builder("authAdminToPortalAllowsSuperUser")
                .superuser(true));
        Portal portal = portalFactory.buildPersisted("authAdminToPortalAllowsSuperUser");
        assertThat(portalService.authAdminToPortal(user, portal.getShortcode()), notNullValue());
    }

    @Test
    public void testGetAll() {
        AdminUser user = adminUserFactory.buildPersisted("authAdminToPortalRejectsUsersNotInPortal");
        assertThat(portalService.findByAdminUserId(user.getId()), hasSize(0));
        Portal portal = portalFactory.buildPersisted("authAdminToPortalRejectsUsersNotInPortal");
        assertThat(portalService.findByAdminUserId(user.getId()), hasSize(0));

        // now add the user to a second portal
        Portal portal2 = portalFactory.buildPersisted("authAdminToPortalRejectsUsersNotInPortal2");
        portalAdminUserService.create(PortalAdminUser.builder()
                .adminUserId(user.getId())
                .portalId(portal2.getId())
                .build());
        // confirm user can access second portal
        List<Portal> portals = portalService.findByAdminUserId(user.getId());
        assertThat(portals, hasSize(1));
        assertThat(portals.get(0).getId(), equalTo(portal2.getId()));
    }
}
