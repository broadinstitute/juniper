package bio.terra.pearl.api.admin.service.siteContent;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.exception.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class SiteImageExtServiceTests extends BaseSpringBootTest {
  @Autowired private AdminUserFactory adminUserFactory;
  @Autowired private PortalFactory portalFactory;
  @Autowired private SiteImageExtService siteImageExtService;

  @Test
  @Transactional
  public void imageListAuthsToPortal(TestInfo testInfo) {
    AdminUser user = adminUserFactory.buildPersisted("imageListAuthsToPortal", false);
    Portal portal = portalFactory.buildPersisted("imageListAuthsToPortal");
    Assertions.assertThrows(
        NotFoundException.class,
        () -> {
          siteImageExtService.list(portal.getShortcode(), user);
        });
  }
}
