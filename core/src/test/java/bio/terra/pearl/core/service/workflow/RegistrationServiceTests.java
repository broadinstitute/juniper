package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.portal.PortalService;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
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
    public void testRegisterWithNoPreReg(TestInfo info) {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
        String portalShortcode = portalService.find(portalEnv.getPortalId()).get().getShortcode();
        String username = "test" + RandomStringUtils.randomAlphabetic(5) + "@test.com";
        RegistrationService.RegistrationResult result = registrationService.register(portalShortcode,
                portalEnv.getEnvironmentName(), username, null);
        Assertions.assertEquals(username, result.participantUser().getUsername());
        Assertions.assertTrue(participantUserService.findOne(username, portalEnv.getEnvironmentName()).isPresent());
    }

    @Test
    @Transactional
    public void testRegisterForGovernedUser() {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted("testRegisterForGovernedUser");
        String portalShortcode = portalService.find(portalEnv.getPortalId()).get().getShortcode();

        String proxyUsername = "test-proxy" + RandomStringUtils.randomAlphabetic(5) + "@test.com";
        RegistrationService.RegistrationResult result = registrationService.register(portalShortcode,
                portalEnv.getEnvironmentName(), proxyUsername, null);

        RegistrationService.RegistrationResult registerGovernedUser = registrationService.registerGovernedUser(portalShortcode,
                result.participantUser());
        Assertions.assertTrue(registerGovernedUser.participantUser().getUsername().contains(proxyUsername));
        Assertions.assertTrue(participantUserService.findOne(proxyUsername, portalEnv.getEnvironmentName()).isPresent());
        Assertions.assertNotEquals(proxyUsername, registerGovernedUser.participantUser().getUsername());
        Assertions.assertTrue(participantUserService.findOne(registerGovernedUser.participantUser().getUsername(), portalEnv.getEnvironmentName()).isPresent());
    }
}
