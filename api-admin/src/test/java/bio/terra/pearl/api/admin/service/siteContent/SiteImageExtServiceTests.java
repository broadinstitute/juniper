package bio.terra.pearl.api.admin.service.siteContent;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.exception.NotFoundException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class SiteImageExtServiceTests extends BaseSpringBootTest {
  @Autowired private AdminUserFactory adminUserFactory;
  @Autowired private PortalFactory portalFactory;
  @Autowired private SiteImageExtService siteImageExtService;

  @Test
  @Transactional
  public void imageListAuthsToPortal() {
    AdminUser operator = adminUserFactory.buildPersisted("imageListAuthsToPortal", false);
    Portal portal = portalFactory.buildPersisted("imageListAuthsToPortal");
    Assertions.assertThrows(
        NotFoundException.class,
        () -> {
          siteImageExtService.list(portal.getShortcode(), operator);
        });
  }

  @Test
  @Transactional
  public void uploadAuthsToPortal() {
    AdminUser operator = adminUserFactory.buildPersisted("uploadAuthsToPortal", false);
    Portal portal = portalFactory.buildPersisted("uploadAuthsToPortal");
    Assertions.assertThrows(
        NotFoundException.class,
        () -> {
          siteImageExtService.upload(
              portal.getShortcode(), "blah", "blah".getBytes(StandardCharsets.UTF_8), operator);
        });
  }
}
