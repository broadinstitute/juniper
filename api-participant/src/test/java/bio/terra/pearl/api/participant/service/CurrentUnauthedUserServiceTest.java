package bio.terra.pearl.api.participant.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import bio.terra.common.exception.NotFoundException;
import bio.terra.common.exception.UnauthorizedException;
import bio.terra.pearl.core.dao.participant.ParticipantUserDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.shared.ApplicationRoutingPaths;
import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class CurrentUnauthedUserServiceTest {
  private final ParticipantUserDao participantUserDao = mock(ParticipantUserDao.class);
  private final CurrentUserService currentUserService = mock(CurrentUserService.class);

  private CurrentUnauthedUserService serviceForZone(String deploymentZone) {
    ApplicationRoutingPaths routingPaths = mock(ApplicationRoutingPaths.class);
    when(routingPaths.getDeploymentZone()).thenReturn(deploymentZone);
    return new CurrentUnauthedUserService(participantUserDao, currentUserService, routingPaths);
  }

  @ParameterizedTest
  @ValueSource(strings = {"prod", "dev"})
  void testLoginDisabledWhenDeployed(String deploymentZone) {
    CurrentUnauthedUserService service = serviceForZone(deploymentZone);
    for (EnvironmentName envName : EnvironmentName.values()) {
      assertThrows(
          NotFoundException.class,
          () -> service.unauthedLogin("someone@test.com", "demo", envName));
    }
    verify(participantUserDao, never()).findOne(any(), any());
  }

  @ParameterizedTest
  @ValueSource(strings = {"prod", "dev"})
  void testRefreshDisabledWhenDeployed(String deploymentZone) {
    CurrentUnauthedUserService service = serviceForZone(deploymentZone);
    for (EnvironmentName envName : EnvironmentName.values()) {
      assertThrows(
          NotFoundException.class, () -> service.unauthedRefresh("fakeToken", "demo", envName));
    }
    verify(currentUserService, never()).refresh(any(), any(), any());
  }

  @ParameterizedTest
  @EnumSource(EnvironmentName.class)
  void testAllowedLocally(EnvironmentName envName) {
    CurrentUnauthedUserService service = serviceForZone("local");
    when(participantUserDao.findOne(any(), any())).thenReturn(Optional.empty());
    // gets past the guard to the user lookup, which fails since the user doesn't exist
    assertThrows(
        UnauthorizedException.class,
        () -> service.unauthedLogin("someone@test.com", "demo", envName));
  }
}
