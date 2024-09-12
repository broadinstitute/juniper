package bio.terra.pearl.core.service.portal;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.admin.PortalAdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.admin.PortalAdminUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

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
    @Transactional
    public void testGetAll(TestInfo info) {
        AdminUser user = adminUserFactory.buildPersisted(getTestName(info));
        assertThat(portalService.findByAdminUser(user), hasSize(0));
        Portal portal = portalFactory.buildPersisted(getTestName(info));
        assertThat(portalService.findByAdminUser(user), hasSize(0));

        // now add the user to a second portal
        Portal portal2 = portalFactory.buildPersisted(getTestName(info));
        portalAdminUserService.create(PortalAdminUser.builder()
                .adminUserId(user.getId())
                .portalId(portal2.getId())
                .build(), getAuditInfo(info));
        // confirm user can access second portal
        List<Portal> portals = portalService.findByAdminUser(user);
        assertThat(portals, hasSize(1));
        assertThat(portals.get(0).getId(), equalTo(portal2.getId()));

        //confirm superuser can access both
        AdminUser superuser = adminUserFactory.buildPersisted(
                adminUserFactory.builder(getTestName(info))
                        .superuser(true));
        assertThat(portalService.findByAdminUser(superuser), hasItems(portal, portal2));
    }
}
