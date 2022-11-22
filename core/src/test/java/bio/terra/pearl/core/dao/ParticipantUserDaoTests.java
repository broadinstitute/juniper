package bio.terra.pearl.core.dao;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoHolder;
import bio.terra.pearl.core.factory.ParticipantUserFactory;
import bio.terra.pearl.core.model.ParticipantUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ParticipantUserDaoTests extends BaseSpringBootTest {
    @Autowired
    ParticipantUserDao participantUserDao;

    @Autowired
    DaoHolder daoHolder;

    @Test
    @Transactional
    public void testCreateUser() {
        ParticipantUser user = ParticipantUserFactory.builderWithDependencies(daoHolder).build();
        ParticipantUser createdUser = participantUserDao.create(user);
        assertNotNull(createdUser.getId(), "Id not attached to generated object");
        assertEquals(user.getUsername(), createdUser.getUsername());
    }



}
