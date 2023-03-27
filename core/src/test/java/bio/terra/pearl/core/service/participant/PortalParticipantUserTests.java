package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.DaoTestUtils;
import bio.terra.pearl.core.factory.PortalEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.ParticipantUserFactory;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class PortalParticipantUserTests extends BaseSpringBootTest {
    @Autowired
    ParticipantUserFactory participantUserFactory;
    @Autowired
    PortalEnvironmentFactory portalEnvFactory;
    @Autowired
    PortalParticipantUserService portalParticipantUserService;

    @Test
    @Transactional
    public void testCrud() {
        PortalEnvironment portalEnv = portalEnvFactory.buildPersisted("ppUserTestCrud");
        ParticipantUser user = participantUserFactory.buildPersisted(portalEnv.getEnvironmentName(), "ppUserTestCrud");
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
