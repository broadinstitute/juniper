package bio.terra.pearl.core.service;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.MailingAddressFactory;
import bio.terra.pearl.core.factory.ParticipantUserFactory;
import bio.terra.pearl.core.factory.ProfileFactory;
import bio.terra.pearl.core.model.participant.MailingAddress;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
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
        MailingAddress address = mailingAddressFactory.builder("testCreateParticipant").build();
        Profile profile =  profileFactory.builder("testCreateParticipant")
                .mailingAddress(address).build();
        ParticipantUser user = participantUserFactory.builderWithDependencies("testCreateParticipant")
                .profile(profile)
                .build();
        ParticipantUser savedUser = participantUserService.create(user);
        Assertions.assertNotNull(savedUser.getProfile().getId());
        Assertions.assertEquals(profile.getContactEmail(), savedUser.getProfile().getContactEmail());
        Assertions.assertNotNull(savedUser.getProfile().getMailingAddress().getId());
        Assertions.assertEquals(address.getCity(), savedUser.getProfile().getMailingAddress().getCity());
    }
}
