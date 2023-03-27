package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.participant.ParticipantUserFactory;
import bio.terra.pearl.core.factory.participant.ProfileFactory;
import bio.terra.pearl.core.model.Environment;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

public class ParticipantUserDaoTests extends BaseSpringBootTest {
    @Autowired
    ParticipantUserDao participantUserDao;
    @Autowired
    ProfileDao profileDao;

    @Autowired
    private ParticipantUserFactory participantUserFactory;
    @Autowired
    private ProfileFactory profileFactory;

    @Test
    @Transactional
    public void testCreateUser() {
        ParticipantUser user = participantUserFactory.builderWithDependencies("testCreateUser").build();
        ParticipantUser createdUser = participantUserDao.create(user);
        assertNotNull(createdUser.getId(), "Id not attached to generated object");
        assertEquals(user.getUsername(), createdUser.getUsername());
    }

    @Test
    @Transactional
    public void testUserUniqueness() {
        ParticipantUser user = participantUserFactory.builderWithDependencies("testCreateUser").build();
        Environment environment = user.getEnvironment();
        ParticipantUser createdUser = participantUserDao.create(user);
        assertNotNull(createdUser.getId(), "Id not attached to generated object");

        ParticipantUser dupeUser = participantUserFactory.builder("testCreateUser")
                .username(user.getUsername())
                .environment(environment).build();
        Exception e = assertThrows(UnableToExecuteStatementException.class, () -> {
            participantUserDao.create(dupeUser);
        });
        assertTrue(e.getMessage().contains("duplicate key value"));
    }

}
