package bio.terra.pearl.api.participant.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import bio.terra.pearl.api.participant.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentBundle;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeAndProxy;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.ParticipantUserFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
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
  @Autowired PortalParticipantUserService portalParticipantUserService;
  @Autowired ParticipantUserFactory participantUserFactory;
  @Autowired PortalEnvironmentFactory portalEnvironmentFactory;
  @Autowired StudyEnvironmentFactory studyEnvironmentFactory;
  @Autowired PortalService portalService;
  @Autowired EnrolleeFactory enrolleeFactory;

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

    CurrentUserService.UserLoginDto loadedUser =
        currentUserService.tokenLogin(token, portalShortcode, portalEnv.getEnvironmentName());
    assertThat(loadedUser.user().getUsername(), equalTo(userBundle.user().getUsername()));
    assertThat(loadedUser.user().getPortalParticipantUsers(), hasSize(1));

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
  public void testRegisterOrLoginNewUser(TestInfo info) {
    PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
    String portalShortcode = portalService.find(portalEnv.getPortalId()).get().getShortcode();
    String username = getTestName(info) + "@test.er" + RandomStringUtils.randomAlphabetic(4);
    String token = generateFakeJwtToken(username);

    CurrentUserService.UserLoginDto loadedUser =
        currentUserService.registerOrLogin(
            token, portalShortcode, portalEnv.getEnvironmentName(), null, "en");
    // confirm new user is created
    assertThat(loadedUser.user().getUsername(), equalTo(username));
    assertThat(loadedUser.user().getPortalParticipantUsers(), hasSize(1));
  }

  @Test
  @Transactional
  public void testRegisterOrLoginExistingUser(TestInfo info) {
    PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
    ParticipantUserFactory.ParticipantUserAndPortalUser userBundle =
        participantUserFactory.buildPersisted(portalEnv, getTestName(info));
    String portalShortcode = portalService.find(portalEnv.getPortalId()).get().getShortcode();
    String token = generateFakeJwtToken(userBundle.user().getUsername());

    CurrentUserService.UserLoginDto loadedUser =
        currentUserService.tokenLogin(token, portalShortcode, portalEnv.getEnvironmentName());
    assertThat(loadedUser.user().getUsername(), equalTo(userBundle.user().getUsername()));
    assertThat(loadedUser.user().getPortalParticipantUsers(), hasSize(1));
  }

  @Test
  @Transactional
  public void testUserLoginWithEnrollees(TestInfo info) {
    StudyEnvironmentBundle studyEnvBundle =
        studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb);
    EnrolleeBundle enrolleeBundle =
        enrolleeFactory.buildWithPortalUser(
            getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());

    String token = generateFakeJwtToken(enrolleeBundle.participantUser().getUsername());

    // see if we can login with the user while they have one enrollee
    CurrentUserService.UserLoginDto loadedUser =
        currentUserService.tokenLogin(
            token,
            studyEnvBundle.getPortal().getShortcode(),
            studyEnvBundle.getStudyEnv().getEnvironmentName());
    assertThat(
        loadedUser.user().getUsername(), equalTo(enrolleeBundle.participantUser().getUsername()));
    assertThat(loadedUser.user().getPortalParticipantUsers(), hasSize(1));
    assertThat(loadedUser.enrollees(), hasSize(1));

    // now see if we can do it with two enrollees attached to the user
    StudyEnvironment studyEnv2 =
        studyEnvironmentFactory.buildPersisted(studyEnvBundle.getPortalEnv(), getTestName(info));
    Enrollee enrollee2 =
        enrolleeFactory.buildPersisted(
            getTestName(info),
            studyEnv2.getId(),
            enrolleeBundle.participantUser().getId(),
            enrolleeBundle.portalParticipantUser().getProfileId());
    loadedUser =
        currentUserService.tokenLogin(
            token,
            studyEnvBundle.getPortal().getShortcode(),
            studyEnvBundle.getStudyEnv().getEnvironmentName());
    assertThat(
        loadedUser.user().getUsername(), equalTo(enrolleeBundle.participantUser().getUsername()));
    assertThat(loadedUser.enrollees(), hasSize(2));
  }

  @Test
  @Transactional
  public void testUserLoginWithProxy(TestInfo info) {
    String email = "proxy" + RandomStringUtils.randomAlphabetic(5) + "@test.com";
    EnrolleeAndProxy enrolleeAndProxy =
        enrolleeFactory.buildProxyAndGovernedEnrollee(getTestName(info), email);
    String token = generateFakeJwtToken(email);
    String portalShortcode =
        portalService.find(enrolleeAndProxy.portalEnv().getPortalId()).get().getShortcode();
    // see if we can login with the user while they have one enrollee
    CurrentUserService.UserLoginDto loadedUser =
        currentUserService.tokenLogin(
            token, portalShortcode, enrolleeAndProxy.portalEnv().getEnvironmentName());
    assertThat(loadedUser.user().getUsername(), equalTo(email));
    assertThat(loadedUser.user().getPortalParticipantUsers(), hasSize(1));
    assertThat(loadedUser.enrollees(), hasSize(2)); // includes proxy enrollees
    assertThat(loadedUser.proxyRelations(), hasSize(1));
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
