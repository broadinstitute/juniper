package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.participant.ParticipantUserFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

public class PortalParticipantUserTests extends BaseSpringBootTest {
    @Autowired
    ParticipantUserFactory participantUserFactory;
    @Autowired
    PortalEnvironmentFactory portalEnvFactory;
    @Autowired
    PortalParticipantUserService portalParticipantUserService;

    @Test
    @Transactional
    public void testCrud(TestInfo info) {
        PortalEnvironment portalEnv = portalEnvFactory.buildPersisted(getTestName(info));
        ParticipantUser user = participantUserFactory.buildPersisted(portalEnv.getEnvironmentName(), getTestName(info));
        assertThat(portalParticipantUserService.findByParticipantUserId(user.getId()), hasSize(0));
        PortalParticipantUser ppUser = PortalParticipantUser.builder()
                .portalEnvironmentId(portalEnv.getId())
                .participantUserId(user.getId()).build();
        PortalParticipantUser savedPpUser = portalParticipantUserService.create(ppUser);
        DaoTestUtils.assertGeneratedProperties(savedPpUser);
        assertThat(savedPpUser.getProfile(), notNullValue());

        assertThat(portalParticipantUserService.findByParticipantUserId(user.getId()), hasSize(1));
    }
}
