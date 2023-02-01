package bio.terra.pearl.core.service;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.PortalEnvironmentFactory;
import bio.terra.pearl.core.factory.survey.ParsedSnapshotFactory;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.survey.ParsedSnapshot;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.workflow.RegistrationService;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class RegistrationServiceTests extends BaseSpringBootTest {
    @Autowired
    private PortalEnvironmentFactory portalEnvironmentFactory;
    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private ParticipantUserService participantUserService;
    @Autowired
    private PortalService portalService;

    @Test
    @Transactional
    public void testRegisterWithNoPreReg() {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted("testRegisterWithNoPreReg");
        String portalShortcode = portalService.find(portalEnv.getPortalId()).get().getShortcode();
        String username = "test" + RandomStringUtils.randomAlphabetic(5) + "@test.com";
        ParsedSnapshot snapshot = ParsedSnapshotFactory.fromMap(Map.of(
                "reg_firstName", "TestFirstName",
                "reg_lastName", "TestLastName",
                "reg_email", username
        ));
        RegistrationService.RegistrationResult result = registrationService.register(portalShortcode,
                portalEnv.getEnvironmentName(), snapshot, null);
        Assertions.assertEquals(username, result.participantUser().getUsername());
        Assertions.assertTrue(participantUserService.findOne(username, portalEnv.getEnvironmentName()).isPresent());
        Assertions.assertEquals("TestFirstName", result.portalParticipantUser().getProfile().getGivenName());
    }
}
