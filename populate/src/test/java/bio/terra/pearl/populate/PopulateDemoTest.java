package bio.terra.pearl.populate;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/** confirm demo portal populates as expected */
public class PopulateDemoTest extends BasePopulatePortalsTest {
    @Test
    @Transactional
    public void testPopulateDemo() throws IOException {
        setUpEnvironments();
        Portal portal = portalPopulator.populate(new FilePopulateContext("portals/demo/portal.json"), true);
        Assertions.assertEquals("demo", portal.getShortcode());
        PortalEnvironment sandbox = portalEnvironmentService.findOne("demo", EnvironmentName.sandbox).get();
        assertThat(portal.getPortalStudies(), hasSize(1));
        Study mainStudy = portal.getPortalStudies().stream().findFirst().get().getStudy();
        Set<StudyEnvironment> studyEnvs = studyEnvironmentService.findByStudy(mainStudy.getId());
        Assertions.assertEquals(3, studyEnvs.size());
        UUID sandboxEnvironmentId = studyEnvs.stream().filter(
                        sEnv -> sEnv.getEnvironmentName().equals(EnvironmentName.sandbox))
                .findFirst().get().getId();

        List<Enrollee> enrollees = enrolleeService.findByStudyEnvironment(sandboxEnvironmentId);
        Assertions.assertEquals(5, enrollees.size());

        checkOldVersionEnrollee(enrollees);
    }

    /** confirm the enrollee wtih answers to two different survey versions was populated correctly */
    private void checkOldVersionEnrollee(List<Enrollee> sandboxEnrollees) {
        Enrollee enrollee = sandboxEnrollees.stream().filter(sandboxEnrollee -> "HDVERS".equals(sandboxEnrollee.getShortcode()))
                .findFirst().get();

        List<Answer> socialHealthAnswers = answerService.findAll(enrollee.getId(), "hd_hd_socialHealth");
        assertThat(socialHealthAnswers, hasSize(3));
        assertThat(socialHealthAnswers, hasItem(
                Matchers.both(hasProperty("questionStableId", equalTo("hd_hd_socialHealth_neighborhoodGetsAlong")))
                        .and(hasProperty("surveyVersion", equalTo(1))))
        );
        assertThat(socialHealthAnswers, hasItem(
                Matchers.both(hasProperty("questionStableId", equalTo("hd_hd_socialHealth_neighborhoodIsNoisy")))
                        .and(hasProperty("surveyVersion", equalTo(2))))
        );
    }
}
