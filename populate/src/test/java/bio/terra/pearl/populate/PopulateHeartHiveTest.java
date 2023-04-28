package bio.terra.pearl.populate;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

public class PopulateHeartHiveTest extends BasePopulatePortalsTest {

    @Test
    @Transactional
    public void testPopulateHeartHive() throws IOException {
        setUpEnvironments();
        Portal portal = portalPopulator.populate(new FilePopulateContext("portals/hearthive/portal.json"), true);
        Assertions.assertEquals("hearthive", portal.getShortcode());
        PortalEnvironment sandbox = portalEnvironmentService.findOne("hearthive", EnvironmentName.sandbox).get();
        assertThat(sandbox.getPreRegSurveyId(), notNullValue());
        assertThat(portal.getPortalStudies(), hasSize(2));
        Study myopathyStudy = portal.getPortalStudies().stream()
                .filter(portalStudy -> portalStudy.getStudy().getShortcode().equals("cmyop"))
                .findFirst().get().getStudy();
        Set<StudyEnvironment> studyEnvs = studyEnvironmentService.findByStudy(myopathyStudy.getId());
        Assertions.assertEquals(3, studyEnvs.size());
        UUID sandboxEnvironmentId = studyEnvs.stream().filter(
                        sEnv -> sEnv.getEnvironmentName().equals(EnvironmentName.sandbox))
                .findFirst().get().getId();

        List<Enrollee> enrollees = enrolleeService.findByStudyEnvironment(sandboxEnvironmentId);
        Assertions.assertEquals(3, enrollees.size());
        Enrollee gertrude = enrollees.stream().filter(enrollee -> "HHGELI".equals(enrollee.getShortcode()))
                .findFirst().get();
        assertThat(gertrude.getPreEnrollmentResponseId(), notNullValue());

        PortalEnvironment liveEnv = portalEnvironmentService.findOne("hearthive", EnvironmentName.live).get();
        StudyEnvironment liveStudyEnv = studyEnvironmentService.findByStudy(myopathyStudy.getShortcode(), EnvironmentName.live).get();

        enrolleeFactory.buildWithPortalUser("testPopulateHeartHive", liveEnv, liveStudyEnv);
        // confirm we can't populate with overwrite if the liveEnrollee isn't withdrawn
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            portalPopulator.populate(new FilePopulateContext("portals/hearthive/portal.json"), true);
        });
    }
}
