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
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.admin.PortalAdminUserService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.portal.PortalService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
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
  public void authUserToPortalRejectsUsersNotInPortal(TestInfo info) {
    AdminUser user = adminUserFactory.buildPersisted(getTestName(info));
    Portal portal = portalFactory.buildPersisted(getTestName(info));
    Assertions.assertThrows(
        NotFoundException.class,
        () -> {
          authUtilService.authUserToPortal(user, portal.getShortcode());
        });

    // now add the user to a second portal
    Portal portal2 = portalFactory.buildPersisted(getTestName(info));
    portalAdminUserService.create(
        PortalAdminUser.builder().adminUserId(user.getId()).portalId(portal2.getId()).build());
    // confirm user can access second portal
    Portal authedPortal = authUtilService.authUserToPortal(user, portal2.getShortcode());
    assertThat(authedPortal.getId(), equalTo(portal2.getId()));
    assertThat(portalService.checkAdminIsInPortal(user, portal.getId()), equalTo(false));
    assertThat(portalService.checkAdminIsInPortal(user, portal2.getId()), equalTo(true));

    // but still not the first
    Assertions.assertThrows(
        NotFoundException.class,
        () -> {
          authUtilService.authUserToPortal(user, portal.getShortcode());
        });
  }

  @Test
  @Transactional
  public void authUserToPortalRejectsNotFoundPortal(TestInfo info) {
    AdminUser user = adminUserFactory.buildPersisted(getTestName(info));
    Assertions.assertThrows(
        NotFoundException.class,
        () -> {
          authUtilService.authUserToPortal(user, "DOES_NOT_EXIST");
        });
  }

  @Test
  @Transactional
  public void authUserToPortalAllowsSuperUser(TestInfo info) {
    AdminUser user =
        adminUserFactory.buildPersisted(
            adminUserFactory.builder(getTestName(info)).superuser(true));
    Portal portal = portalFactory.buildPersisted(getTestName(info));
    assertThat(authUtilService.authUserToPortal(user, portal.getShortcode()), notNullValue());
  }

  @Test
  public void testVerifyObjToPortal() {
    Portal portal = Portal.builder().id(UUID.randomUUID()).build();
    Assertions.assertThrows(
        NotFoundException.class,
        () -> {
          authUtilService.verifyObjInPortal(portal, Optional.of(new Survey()));
        });
    Assertions.assertThrows(
        NotFoundException.class,
        () -> {
          authUtilService.verifyObjInPortal(portal, Optional.empty());
        });

    Survey survey = Survey.builder().portalId(portal.getId()).build();
    authUtilService.verifyObjInPortal(portal, Optional.of(survey));
  }
}
