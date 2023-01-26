package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.participant.MailingAddressFactory;
import bio.terra.pearl.core.factory.participant.ParticipantUserFactory;
import bio.terra.pearl.core.factory.ProfileFactory;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class ParticipantUserServiceTests extends BaseSpringBootTest {
    @Autowired
    private ParticipantUserService participantUserService;
    @Autowired
    private ProfileFactory profileFactory;
    @Autowired
    private ParticipantUserFactory participantUserFactory;
    @Autowired
    private MailingAddressFactory mailingAddressFactory;


    @Test
    @Transactional
    public void testCreateParticipant() {
        ParticipantUser user = participantUserFactory.builderWithDependencies("testCreateParticipant")
                .build();
        ParticipantUser savedUser = participantUserService.create(user);
        Assertions.assertNotNull(savedUser.getId());
    }
}
