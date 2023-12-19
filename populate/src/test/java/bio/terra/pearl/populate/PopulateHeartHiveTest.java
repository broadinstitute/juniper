package bio.terra.pearl.populate;

import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

public class PopulateHeartHiveTest extends BasePopulatePortalsTest {
    @Autowired
    private TransactionTemplate txTemplate;

    @Test
    @Transactional
    public void testPopulateHeartHive() {
        // manually manage the transaction so that we can add a save point midway through for a rollback
        txTemplate.execute(status -> {
            setUpEnvironments();
            Portal portal = portalPopulator.populate(new FilePopulateContext("portals/hearthive/portal.json"), true);
            Assertions.assertEquals("hearthive", portal.getShortcode());
            PortalEnvironment sandbox = portalEnvironmentService.findOne("hearthive", EnvironmentName.sandbox).get();
            assertThat(portal.getPortalStudies(), hasSize(3));
            Study myopathyStudy = portal.getPortalStudies().stream()
                    .filter(portalStudy -> portalStudy.getStudy().getShortcode().equals("hh_registry"))
                    .findFirst().get().getStudy();
            List<StudyEnvironment> studyEnvs = studyEnvironmentService.findByStudy(myopathyStudy.getId());
            Assertions.assertEquals(3, studyEnvs.size());
            UUID sandboxEnvironmentId = studyEnvs.stream().filter(
                            sEnv -> sEnv.getEnvironmentName().equals(EnvironmentName.sandbox))
                    .findFirst().get().getId();

            List<Enrollee> enrollees = enrolleeService.findByStudyEnvironment(sandboxEnvironmentId);
            Assertions.assertEquals(3, enrollees.size());
            enrollees.stream().filter(enrollee -> "HHGELI".equals(enrollee.getShortcode()))
                    .findFirst().orElseThrow(() -> new NotFoundException("enrollee HHGELI not found"));

            PortalEnvironment liveEnv = portalEnvironmentService.findOne("hearthive", EnvironmentName.live).get();
            StudyEnvironment liveStudyEnv = studyEnvironmentService.findByStudy(myopathyStudy.getShortcode(), EnvironmentName.live).get();

            EnrolleeFactory.EnrolleeBundle prodEnrollee = enrolleeFactory.buildWithPortalUser("testPopulateHeartHive", liveEnv, liveStudyEnv);
            // confirm we can't populate with overwrite if the liveEnrollee isn't withdrawn
            var savepoint = status.createSavepoint();
            Assertions.assertThrows(UnsupportedOperationException.class, () -> {
                portalPopulator.populate(new FilePopulateContext("portals/hearthive/portal.json"), true);
            });
            // the failed populate won't roll back since we're inside a test-wide transaction, so roll it back manually
            status.rollbackToSavepoint(savepoint);

            withdrawnEnrolleeService.withdrawEnrollee(prodEnrollee.enrollee());
            // confirm we can now repopulate with overwrite
            portalPopulator.populate(new FilePopulateContext("portals/hearthive/portal.json"), true);
            return null;
        });

        withdrawnEnrolleeService.withdrawEnrollee(prodEnrollee.enrollee());
        // confirm we can now repopulate with overwrite
        portalPopulator.populate(new FilePopulateContext("portals/hearthive/portal.json"), true);
    }
}
