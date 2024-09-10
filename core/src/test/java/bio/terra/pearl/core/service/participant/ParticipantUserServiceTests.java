package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.participant.ParticipantUserFactory;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class ParticipantUserServiceTests extends BaseSpringBootTest {
    @Autowired
    private ParticipantUserService participantUserService;
    @Autowired
    private ParticipantUserFactory participantUserFactory;


    @Test
    @Transactional
    public void testCreateParticipant(TestInfo info) {
        ParticipantUser user = participantUserFactory.builderWithDependencies(getTestName(info))
                .build();
        ParticipantUser savedUser = participantUserService.create(user);
        Assertions.assertNotNull(savedUser.getShortcode());
        Assertions.assertNotNull(savedUser.getId());
    }
}
