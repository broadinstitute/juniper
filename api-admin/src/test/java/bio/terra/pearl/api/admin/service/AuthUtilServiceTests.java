package bio.terra.pearl.api.admin.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.admin.PortalAdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.admin.PortalAdminUserService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.portal.PortalService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class AuthUtilServiceTests extends BaseSpringBootTest {
  @Autowired private AuthUtilService authUtilService;
  @Autowired private PortalService portalService;
  @Autowired private PortalAdminUserService portalAdminUserService;
  @Autowired private AdminUserFactory adminUserFactory;
  @Autowired private PortalFactory portalFactory;

  @Test
  @Transactional
  public void authAdminToPortalRejectsUsersNotInPortal() {
    AdminUser user = adminUserFactory.buildPersisted("authAdminToPortalRejectsUsersNotInPortal");
    Portal portal = portalFactory.buildPersisted("authAdminToPortalRejectsUsersNotInPortal");
    Assertions.assertThrows(
        NotFoundException.class,
        () -> {
          authUtilService.authAdminToPortal(user, portal.getShortcode());
        });

    // now add the user to a second portal
    Portal portal2 = portalFactory.buildPersisted("authAdminToPortalRejectsUsersNotInPortal2");
    portalAdminUserService.create(
        PortalAdminUser.builder().adminUserId(user.getId()).portalId(portal2.getId()).build());
    // confirm user can access second portal
    Portal authedPortal = authUtilService.authAdminToPortal(user, portal2.getShortcode());
    assertThat(authedPortal.getId(), equalTo(portal2.getId()));
    assertThat(portalService.checkAdminIsInPortal(user, portal.getId()), equalTo(false));
    assertThat(portalService.checkAdminIsInPortal(user, portal2.getId()), equalTo(true));

    // but still not the first
    Assertions.assertThrows(
        NotFoundException.class,
        () -> {
          authUtilService.authAdminToPortal(user, portal.getShortcode());
        });
  }

  @Test
  @Transactional
  public void authAdminToPortalRejectsNotFoundPortal() {
    AdminUser user = adminUserFactory.buildPersisted("authAdminToPortalRejectsNotFoundPortal");
    Assertions.assertThrows(
        NotFoundException.class,
        () -> {
          authUtilService.authAdminToPortal(user, "DOES_NOT_EXIST");
        });
  }

  @Test
  @Transactional
  public void authAdminToPortalAllowsSuperUser() {
    AdminUser user =
        adminUserFactory.buildPersisted(
            adminUserFactory.builder("authAdminToPortalAllowsSuperUser").superuser(true));
    Portal portal = portalFactory.buildPersisted("authAdminToPortalAllowsSuperUser");
    assertThat(authUtilService.authAdminToPortal(user, portal.getShortcode()), notNullValue());
  }
}
