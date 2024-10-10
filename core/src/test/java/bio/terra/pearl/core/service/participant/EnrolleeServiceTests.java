package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.StudyEnvironmentBundle;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.CascadeProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

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
    public void testEnrolleeCreate(TestInfo info) {
        Enrollee enrollee = enrolleeFactory.builderWithDependencies(getTestName(info)).build();
        Enrollee savedEnrollee = enrolleeService.create(enrollee);
        DaoTestUtils.assertGeneratedProperties(savedEnrollee);
        Assertions.assertNotNull(savedEnrollee.getShortcode());
        Assertions.assertEquals(enrollee.getParticipantUserId(), savedEnrollee.getParticipantUserId());
    }

    @Test
    @Transactional
    public void testEnrolleeDelete(TestInfo info) {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info), EnvironmentName.irb);
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));
        EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv);

        enrolleeService.delete(enrolleeBundle.enrollee().getId(), CascadeProperty.EMPTY_SET);
        assertThat(enrolleeService.find(enrolleeBundle.enrollee().getId()).isPresent(), equalTo(false));
    }

    @Test
    @Transactional
    public void testEnrolleeCannotDeleteLive(TestInfo info) {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info), EnvironmentName.live);
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));
        EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv);

        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            enrolleeService.delete(enrolleeBundle.enrollee().getId(), CascadeProperty.EMPTY_SET);
        });
        assertThat(enrolleeService.find(enrolleeBundle.enrollee().getId()).isPresent(), equalTo(true));
    }

    @Test
    @Transactional
    public void testFindByShortcodeAndStudyEnv(TestInfo info) {
        StudyEnvironmentBundle bundle1 = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        StudyEnvironment studyEnv1 = bundle1.getStudyEnv();
        Study study = bundle1.getStudy();
        PortalEnvironment portalEnv1 = bundle1.getPortalEnv();

        StudyEnvironmentBundle bundle2 = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);


        EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv1, studyEnv1);

        Enrollee foundEnrollee = enrolleeService
                .findByShortcodeAndStudyEnv(enrolleeBundle.enrollee().getShortcode(), study.getShortcode(), EnvironmentName.sandbox)
                .orElseThrow();
        assertThat(foundEnrollee.getId(), equalTo(enrolleeBundle.enrollee().getId()));

        Assertions.assertFalse(enrolleeService
                .findByShortcodeAndStudyEnv(enrolleeBundle.enrollee().getShortcode(), study.getShortcode(), EnvironmentName.live)
                .isPresent());

        Assertions.assertFalse(enrolleeService
                .findByShortcodeAndStudyEnv(enrolleeBundle.enrollee().getShortcode(), bundle2.getStudy().getShortcode(), EnvironmentName.sandbox)
                .isPresent());
    }
}
