package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentBundle;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.ParticipantUserFactory;
import bio.terra.pearl.core.factory.participant.ProfileFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParticipantUserDaoTests extends BaseSpringBootTest {
    @Autowired
    ParticipantUserDao participantUserDao;
    @Autowired
    ProfileDao profileDao;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    private EnrolleeFactory enrolleeFactory;

    @Autowired
    private ParticipantUserFactory participantUserFactory;
    @Autowired
    private ProfileFactory profileFactory;

    @Test
    @Transactional
    public void testCreateUser(TestInfo info) {
        ParticipantUser user = participantUserFactory.builderWithDependencies(getTestName(info)).build();
        ParticipantUser createdUser = participantUserDao.create(user);
        assertNotNull(createdUser.getId(), "Id not attached to generated object");
        assertEquals(user.getUsername(), createdUser.getUsername());
    }

    @Test
    @Transactional
    public void testUserUniqueness(TestInfo info) {
        ParticipantUser user = participantUserFactory.builderWithDependencies(getTestName(info)).build();
        EnvironmentName environment = user.getEnvironmentName();
        ParticipantUser createdUser = participantUserDao.create(user);
        assertNotNull(createdUser.getId(), "Id not attached to generated object");

        ParticipantUser dupeUser = participantUserFactory.builder(getTestName(info))
                .username(user.getUsername())
                .environmentName(environment).build();
        Exception e = assertThrows(UnableToExecuteStatementException.class, () -> {
            participantUserDao.create(dupeUser);
        });
        assertTrue(e.getMessage().contains("duplicate key value"));
    }

    @Test
    @Transactional
    public void testFindAllByPortalEnv(TestInfo testInfo) {
        StudyEnvironmentBundle sandboxBundle = studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);
        EnrolleeBundle sandbox = enrolleeFactory.buildWithPortalUser(getTestName(testInfo), sandboxBundle.getPortalEnv(), sandboxBundle.getStudyEnv());
        EnrolleeBundle sandbox2 = enrolleeFactory.buildWithPortalUser(getTestName(testInfo), sandboxBundle.getPortalEnv(), sandboxBundle.getStudyEnv());

        StudyEnvironmentBundle irbBundle = studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.irb, sandboxBundle.getPortal(), sandboxBundle.getStudy());
        EnrolleeBundle irb = enrolleeFactory.buildWithPortalUser(getTestName(testInfo), irbBundle.getPortalEnv(), irbBundle.getStudyEnv());

        StudyEnvironmentBundle sandboxStudy2 = studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox, sandboxBundle.getPortal(), sandboxBundle.getPortalEnv());
        EnrolleeBundle otherSandbox = enrolleeFactory.buildWithPortalUser(getTestName(testInfo), sandboxStudy2.getPortalEnv(), sandboxStudy2.getStudyEnv());

        StudyEnvironmentBundle otherPortalSandboxBundle = studyEnvironmentFactory.buildBundle(getTestName(testInfo), EnvironmentName.sandbox);
        EnrolleeBundle otherPortalEnrollee = enrolleeFactory.buildWithPortalUser(getTestName(testInfo), otherPortalSandboxBundle.getPortalEnv(), otherPortalSandboxBundle.getStudyEnv());

        // gets all the enrollees in the portal env
        List<ParticipantUser> participantUsers = participantUserDao.findAllByPortalEnv(sandboxBundle.getPortal().getId(), EnvironmentName.sandbox);
        assertThat(participantUsers, hasSize(3));
        assertThat(participantUsers.stream().map(ParticipantUser::getId).toList(),
                containsInAnyOrder(sandbox.enrollee().getParticipantUserId(), sandbox2.enrollee().getParticipantUserId(), otherSandbox.enrollee().getParticipantUserId()));
    }


}
