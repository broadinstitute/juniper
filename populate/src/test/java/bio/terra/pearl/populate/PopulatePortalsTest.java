package bio.terra.pearl.populate;

import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.site.HtmlSection;
import bio.terra.pearl.core.model.site.HtmlSectionType;
import bio.terra.pearl.core.model.site.LocalizedSiteContent;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.populate.service.BaseSeedPopulator;
import bio.terra.pearl.populate.service.EnvironmentPopulator;
import bio.terra.pearl.populate.service.PortalPopulator;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Attempts to populate every portal in the seed path.
 * Since the aim of the seed path is to have portals sufficient for a developer/demo-er to easily view
 * all aspects of product functionality.  So this test essentially confirms that a fresh development/demo
 * environment can be created with all functionality accessible.
 */
public class PopulatePortalsTest extends BaseSpringBootTest {
    @Autowired
    private PortalPopulator portalPopulator;
    @Autowired
    private EnvironmentPopulator environmentPopulator;
    @Autowired
    private StudyEnvironmentService studyEnvironmentService;
    @Autowired
    private EnrolleeService enrolleeService;
    @Autowired
    private SurveyService surveyService;
    @Autowired
    private SurveyResponseService surveyResponseService;
    @Autowired
    private PortalEnvironmentService portalEnvironmentService;
    @Autowired
    private EnrolleeFactory enrolleeFactory;

    private void setUpEnvironments() throws IOException {
        for (String fileName : BaseSeedPopulator.ENVIRONMENTS_TO_POPULATE) {
            environmentPopulator.populate(new FilePopulateContext(fileName), true);
        }
    }

    /**
     * populate the ourhealth study and do cursory checks on it.
     * This test will need to be updated as we change/update the prepopulated data.
     * @throws IOException
     */
    @Test
    @Transactional
    public void testPopulateOurHealth() throws IOException {
        setUpEnvironments();
        Portal portal = portalPopulator.populate(new FilePopulateContext("portals/ourhealth/portal.json"), true);
        Assertions.assertEquals("ourhealth", portal.getShortcode());

        Study mainStudy = portal.getPortalStudies().stream().findFirst().get().getStudy();
        Set<StudyEnvironment> studyEnvs = studyEnvironmentService.findByStudy(mainStudy.getId());
        Assertions.assertEquals(3, studyEnvs.size());
        UUID sandboxEnvironmentId = studyEnvs.stream().filter(
                        sEnv -> sEnv.getEnvironmentName().equals(EnvironmentName.sandbox))
                .findFirst().get().getId();

        List<Enrollee> enrollees = enrolleeService.findByStudyEnvironmentAdminLoad(sandboxEnvironmentId);
        Assertions.assertEquals(3, enrollees.size());
        Enrollee jonas = enrollees.stream().filter(enrollee -> "OHSALK".equals(enrollee.getShortcode()))
                .findFirst().get();
        checkOurhealthSurveys(jonas);
        checkOurhealthSiteContent(portal.getId());

        // now check that we can populate it again, to make sure we don't have deletion issues
        portalPopulator.populate(new FilePopulateContext("portals/ourhealth/portal.json"), true);
    }

    private void checkOurhealthSurveys(Enrollee jonas) throws IOException {
        Survey cardioHistorySurvey = surveyService.findByStableId("oh_oh_cardioHx", 1).get();

        List<SurveyResponse> jonasResponses = surveyResponseService.findByEnrolleeId(jonas.getId());
        Assertions.assertEquals(2, jonasResponses.size());
        SurveyResponse medHistoryResp = jonasResponses.stream()
                .filter(response -> cardioHistorySurvey.getId().equals(response.getSurveyId()))
                .findFirst().get();
        SurveyResponse fetchedResponse = surveyResponseService.findOneWithAnswers(medHistoryResp.getId()).get();
        Answer firstAnswer = fetchedResponse.getAnswers().get(0);
        Assertions.assertEquals("yesSpecificallyAboutMyHeart", firstAnswer.getStringValue());
    }

    private void checkOurhealthSiteContent(UUID portalId) {
        PortalEnvironment portalEnv = portalEnvironmentService
                .loadWithParticipantSiteContent("ourhealth", EnvironmentName.sandbox, "en").get();
        Assertions.assertEquals(portalId, portalEnv.getPortalId());
        LocalizedSiteContent lsc = portalEnv.getSiteContent().getLocalizedSiteContents()
                .stream().findFirst().get();
        HtmlSection firstLandingSection = lsc.getLandingPage()
                .getSections().stream().findFirst().get();
        Assertions.assertEquals(HtmlSectionType.HERO_WITH_IMAGE, firstLandingSection.getSectionType());
        Assertions.assertNotNull(lsc.getFooterSection());
    }

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

        List<Enrollee> enrollees = enrolleeService.findByStudyEnvironmentAdminLoad(sandboxEnvironmentId);
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
