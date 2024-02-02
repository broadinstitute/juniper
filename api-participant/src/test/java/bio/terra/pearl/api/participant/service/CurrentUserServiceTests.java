package bio.terra.pearl.api.participant.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import bio.terra.pearl.api.participant.BaseSpringBootTest;
import bio.terra.pearl.core.factory.participant.ParticipantUserFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.service.portal.PortalService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class CurrentUserServiceTests extends BaseSpringBootTest {
  @Autowired CurrentUserService currentUserService;
  @Autowired ParticipantUserFactory participantUserFactory;
  @Autowired PortalEnvironmentFactory portalEnvironmentFactory;
  @Autowired PortalService portalService;

  @Test
  @Transactional
  public void testUnauthorizedIfNoUser() {
    String username = "test" + RandomStringUtils.randomAlphabetic(5) + "@test.com";
    String token = generateFakeJwtToken(username);
    try {
      currentUserService.tokenLogin(token, "whatev", EnvironmentName.irb);
      Assertions.fail("Should have thrown UnauthorizedException");
    } catch (Exception e) {
      assertThat(
          e.getMessage(),
          containsString("User not found for environment %s".formatted(EnvironmentName.irb)));
    }
  }

  @Test
  @Transactional
  public void testUserLoginIfPresent(TestInfo info) {
    PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
    ParticipantUserFactory.ParticipantUserAndPortalUser userBundle =
        participantUserFactory.buildPersisted(portalEnv, getTestName(info));
    String portalShortcode = portalService.find(portalEnv.getPortalId()).get().getShortcode();
    String token = generateFakeJwtToken(userBundle.user().getUsername());

    try {
      CurrentUserService.UserWithEnrollees loadedUser =
          currentUserService.tokenLogin(token, portalShortcode, portalEnv.getEnvironmentName());
      assertThat(loadedUser.user().getUsername(), equalTo(userBundle.user().getUsername()));
      assertThat(loadedUser.user().getPortalParticipantUsers(), hasSize(1));
    } catch (Exception e) {
      Assertions.fail("Unexpected exception: " + e.getMessage());
    }

    String missingUsername = "missing" + RandomStringUtils.randomAlphabetic(5) + "@test.com";
    String missingUserToken = generateFakeJwtToken(missingUsername);
    try {
      currentUserService.tokenLogin(
          missingUserToken, portalShortcode, portalEnv.getEnvironmentName());
      Assertions.fail("Should have thrown UnauthorizedException");
    } catch (Exception e) {
      assertThat(
          e.getMessage(),
          containsString(
              "User not found for environment %s".formatted(portalEnv.getEnvironmentName())));
    }
  }

  @Test
  @Transactional
  public void testUserLoginWrongPortal(TestInfo testInfo) {
    String testName = getTestName(testInfo);
    PortalEnvironment portalEnv1 =
        portalEnvironmentFactory.buildPersisted(testName, EnvironmentName.sandbox);
    ParticipantUserFactory.ParticipantUserAndPortalUser userBundle =
        participantUserFactory.buildPersisted(portalEnv1, testName);

    PortalEnvironment portalEnv2 =
        portalEnvironmentFactory.buildPersisted(testName + "2", EnvironmentName.sandbox);
    String portalShortcode2 = portalService.find(portalEnv2.getPortalId()).get().getShortcode();

    String token = generateFakeJwtToken(userBundle.user().getUsername());
    try {
      currentUserService.tokenLogin(token, portalShortcode2, portalEnv2.getEnvironmentName());
      Assertions.fail("Should have thrown UnauthorizedException");
    } catch (Exception e) {
      assertThat(
          e.getMessage(), equalTo("User not found for portal %s".formatted(portalShortcode2)));
    }
  }

  private String generateFakeJwtToken(String username) {
    UUID token = UUID.randomUUID();
    return JWT.create()
        .withClaim("token", token.toString())
        .withClaim("email", username)
        .sign(Algorithm.none());
  }
}
