package bio.terra.pearl.api.participant.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import bio.terra.pearl.api.participant.BaseSpringBootTest;
import bio.terra.pearl.core.factory.participant.ParticipantUserFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.service.portal.PortalService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class CurrentUserServiceTests extends BaseSpringBootTest {
  @Autowired CurrentUserService currentUserService;
  @Autowired ParticipantUserFactory participantUserFactory;
  @Autowired PortalEnvironmentFactory portalEnvironmentFactory;
  @Autowired PortalService portalService;

  @Test
  @Transactional
  public void testUserLoginEmptyIfNoUser() {
    String username = "test" + RandomStringUtils.randomAlphabetic(5) + "@test.com";
    String token = generateFakeJwtToken(username);
    var userBundle = currentUserService.tokenLogin(token, "whatev", EnvironmentName.irb);
    assertThat(userBundle.isEmpty(), equalTo(true));
  }

  @Test
  @Transactional
  public void testUserLoginIfPresent() {
    var portalEnv = portalEnvironmentFactory.buildPersisted("testUserLogin");
    var userBundle = participantUserFactory.buildPersisted(portalEnv, "testUserLogin");
    String portalShortcode = portalService.find(portalEnv.getPortalId()).get().getShortcode();
    String token = generateFakeJwtToken(userBundle.user().getUsername());

    var loadedUserOpt =
        currentUserService.tokenLogin(token, portalShortcode, portalEnv.getEnvironmentName());

    assertThat(loadedUserOpt.isPresent(), equalTo(true));
    var loadedUser = loadedUserOpt.get();
    assertThat(loadedUser.user().getUsername(), equalTo(userBundle.user().getUsername()));
    assertThat(loadedUser.user().getPortalParticipantUsers(), hasSize(1));

    String missingUsername = "missing" + RandomStringUtils.randomAlphabetic(5) + "@test.com";
    String missingUserToken = generateFakeJwtToken(missingUsername);
    var missingUserOpt =
        currentUserService.tokenLogin(
            missingUserToken, portalShortcode, portalEnv.getEnvironmentName());
    assertThat(missingUserOpt.isEmpty(), equalTo(true));
  }

  String generateFakeJwtToken(String username) {
    var token = UUID.randomUUID();
    return JWT.create()
        .withClaim("token", token.toString())
        .withClaim("email", username)
        .sign(Algorithm.none());
  }
}
