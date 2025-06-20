package bio.terra.pearl.api.admin.service.participant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import bio.terra.pearl.api.admin.AuthAnnotationSpec;
import bio.terra.pearl.api.admin.AuthTestUtils;
import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.service.auth.SuperuserOnly;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnvAuthContext;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.PortalService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class ParticipantUserExtServiceTests extends BaseSpringBootTest {
  @Autowired ParticipantUserExtService participantUserExtService;
  @Autowired EnrolleeFactory enrolleeFactory;
  @Autowired ParticipantUserService participantUserService;
  @Autowired PortalService portalService;
  @Autowired PortalEnvironmentService portalEnvironmentService;
  @Autowired AdminUserFactory adminUserFactory;

  @Test
  public void testAllAuthenticated() {
    AuthTestUtils.assertAllMethodsAnnotated(
        participantUserExtService,
        Map.of(
            "list",
            AuthAnnotationSpec.withPortalEnvPerm("participant_data_view"),
            "findWithPortalUser",
            AuthAnnotationSpec.withPortalEnvPerm("participant_data_view"),
            "update",
            AuthAnnotationSpec.withPortalEnvPerm(
                "participant_data_edit", List.of(SuperuserOnly.class))));
  }

  @Test
  @Transactional
  public void testCanOnlyUpdateUsername(TestInfo info) {

    AdminUser superuser = adminUserFactory.buildPersisted(getTestName(info), true);

    EnrolleeBundle bundle = enrolleeFactory.buildWithPortalUser(getTestName(info));
    ParticipantUser participantUser = bundle.participantUser();

    Portal portal =
        portalService
            .find(bundle.portalId())
            .orElseThrow(() -> new RuntimeException("Portal not found"));

    PortalEnvironment portalEnvironment =
        portalEnvironmentService
            .find(bundle.portalParticipantUser().getPortalEnvironmentId())
            .orElseThrow(() -> new RuntimeException("Portal environment not found"));

    ParticipantUser update =
        participantUserService
            .find(participantUser.getId())
            .orElseThrow(() -> new RuntimeException("Participant user not found"));

    update.setUsername("otheremail@other.net");
    update.setShortcode("ACC_123456789"); // this should be ignored
    update.setId(UUID.randomUUID()); // this should be ignored
    update.setEnvironmentName(EnvironmentName.irb); // this should be ignored
    update.setLastUpdatedAt(Instant.MIN);
    update.setCreatedAt(Instant.MIN);
    update.setLastLogin(Instant.MIN);

    ParticipantUser updated =
        participantUserExtService.update(
            PortalEnvAuthContext.of(
                superuser, portal.getShortcode(), portalEnvironment.getEnvironmentName()),
            participantUser.getId(),
            update);

    assertEquals("otheremail@other.net", updated.getUsername());
    assertEquals(participantUser.getShortcode(), updated.getShortcode());
    assertEquals(participantUser.getId(), updated.getId());
    assertEquals(participantUser.getEnvironmentName(), updated.getEnvironmentName());
    assertNotEquals(participantUser.getLastUpdatedAt(), Instant.MIN);
    assertEquals(participantUser.getCreatedAt(), updated.getCreatedAt());
    assertEquals(participantUser.getLastLogin(), updated.getLastLogin());
  }
}
