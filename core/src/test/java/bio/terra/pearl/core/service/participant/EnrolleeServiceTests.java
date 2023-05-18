package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.CascadeProperty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class EnrolleeServiceTests extends BaseSpringBootTest {
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private EnrolleeService enrolleeService;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    private PortalEnvironmentFactory portalEnvironmentFactory;

    @Test
    @Transactional
    public void testEnrolleeCreate() {
        Enrollee enrollee = enrolleeFactory.builderWithDependencies("testEnrolleeCrud").build();
        Enrollee savedEnrollee = enrolleeService.create(enrollee);
        DaoTestUtils.assertGeneratedProperties(savedEnrollee);
        Assertions.assertNotNull(savedEnrollee.getShortcode());
        Assertions.assertEquals(enrollee.getParticipantUserId(), savedEnrollee.getParticipantUserId());
    }

    @Test
    @Transactional
    public void testEnrolleeDelete() {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted("testFindByStatusAndTimeMulti", EnvironmentName.irb );
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, "testFindByStatusAndTimeMulti");
        var enrolleeBundle = enrolleeFactory.buildWithPortalUser("testFindByStatusAndTimeMulti", portalEnv, studyEnv);

        enrolleeService.delete(enrolleeBundle.enrollee().getId(), CascadeProperty.EMPTY_SET);
        assertThat(enrolleeService.find(enrolleeBundle.enrollee().getId()).isPresent(), equalTo(false));
    }

    @Test
    @Transactional
    public void testEnrolleeCannotDeleteLive() {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted("testFindByStatusAndTimeMulti", EnvironmentName.live );
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, "testFindByStatusAndTimeMulti");
        var enrolleeBundle = enrolleeFactory.buildWithPortalUser("testFindByStatusAndTimeMulti", portalEnv, studyEnv);

        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            enrolleeService.delete(enrolleeBundle.enrollee().getId(), CascadeProperty.EMPTY_SET);
        });
        assertThat(enrolleeService.find(enrolleeBundle.enrollee().getId()).isPresent(), equalTo(true));
    }
}
