package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentBundle;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
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
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private ParticipantUserService participantUserService;
    @Autowired
    private PortalService portalService;
    @Autowired
    private EnrolleeFactory enrolleeFactory;


    @Test
    @Transactional
    public void testRegisterWithNoPreReg(TestInfo info) {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
        String portalShortcode = portalService.find(portalEnv.getPortalId()).get().getShortcode();
        String username = "test" + RandomStringUtils.randomAlphabetic(5) + "@test.com";
        RegistrationService.RegistrationResult result = registrationService.register(portalShortcode,
                portalEnv.getEnvironmentName(), username, null, null);
        Assertions.assertEquals(username, result.participantUser().getUsername());
        Assertions.assertTrue(participantUserService.findOne(username, portalEnv.getEnvironmentName()).isPresent());
    }

    @Test
    @Transactional
    public void testRegisterForGovernedUser(TestInfo info) {
        StudyEnvironmentBundle bundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);

        EnrolleeBundle proxyBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), bundle.getPortalEnv(), bundle.getStudyEnv());
        ParticipantUser proxyUser = participantUserService.find(proxyBundle.enrollee().getParticipantUserId()).orElseThrow();
        RegistrationService.RegistrationResult registerGovernedUser = registrationService.registerGovernedUser(proxyUser, proxyBundle.portalParticipantUser(), proxyUser.getUsername()+"-prox-RFGU", null);
        Assertions.assertTrue(registerGovernedUser.participantUser().getUsername().contains(proxyUser.getUsername()));
        Assertions.assertTrue(participantUserService.findOne(proxyUser.getUsername(), bundle.getPortalEnv().getEnvironmentName()).isPresent());
        Assertions.assertNotEquals(proxyUser.getUsername(), registerGovernedUser.participantUser().getUsername());
        Assertions.assertTrue(participantUserService.findOne(registerGovernedUser.participantUser().getUsername(), bundle.getPortalEnv().getEnvironmentName()).isPresent());
    }

    @Test
    @Transactional
    public void testRegisterWithPreferredLanguage(TestInfo info) {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
        String portalShortcode = portalService.find(portalEnv.getPortalId()).get().getShortcode();
        String username = "test" + RandomStringUtils.randomAlphabetic(5) + "@test.com";
        RegistrationService.RegistrationResult result = registrationService.register(portalShortcode,
                portalEnv.getEnvironmentName(), username, null, "es");
        Assertions.assertEquals("es", result.profile().getPreferredLanguage());
    }

    @Test
    @Transactional
    public void testRegisterWithDefaultLanguage(TestInfo info) {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
        String portalShortcode = portalService.find(portalEnv.getPortalId()).get().getShortcode();
        String username = "test" + RandomStringUtils.randomAlphabetic(5) + "@test.com";
        RegistrationService.RegistrationResult result = registrationService.register(portalShortcode,
                portalEnv.getEnvironmentName(), username, null, null);
        Assertions.assertEquals(portalEnv.getPortalEnvironmentConfig().getDefaultLanguage(), result.profile().getPreferredLanguage());
    }
}
