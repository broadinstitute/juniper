package bio.terra.pearl.core.service.study;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.PortalEnvironmentFactory;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.ParticipantUserFactory;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.PortalService;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class EnrollmentServiceTests extends BaseSpringBootTest {
    @Test
    @Transactional
    public void testEnroll() {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted("testEnroll");
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, "testEnroll");
        ParticipantUser user = participantUserFactory.buildPersisted(studyEnv.getEnvironmentName(), "testEnroll");
        PortalParticipantUser ppUser = PortalParticipantUser.builder()
                .participantUserId(user.getId())
                .portalEnvironmentId(portalEnv.getId()).build();
        // enrollment requires an already-existing portalParticipantUser
        portalParticipantUserService.create(ppUser);

        String portalShortcode = portalService.find(portalEnv.getPortalId()).get().getShortcode();
        String studyShortcode = studyService.find(studyEnv.getStudyId()).get().getShortcode();

        Enrollee enrollee = enrollmentService.enroll(user, portalShortcode, studyEnv.getEnvironmentName(), studyShortcode, null);

        assertThat(enrollee.getShortcode(), notNullValue());
        assertThat(enrollee.getParticipantUserId(), equalTo(user.getId()));

        assertThat(enrolleeService.findByStudyEnvironment(studyEnv.getId()), contains(enrollee));
    }

    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;

    @Autowired
    private ParticipantUserFactory participantUserFactory;

    @Autowired
    private PortalStudyService portalStudyService;
    @Autowired
    private PortalService portalService;
    @Autowired
    private StudyService studyService;
    @Autowired
    private PortalEnvironmentService portalEnvironmentService;
    @Autowired
    private EnrolleeService enrolleeService;
    @Autowired
    private PortalParticipantUserService portalParticipantUserService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private PortalEnvironmentFactory portalEnvironmentFactory;
}
