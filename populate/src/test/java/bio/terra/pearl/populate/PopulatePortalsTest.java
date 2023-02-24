package bio.terra.pearl.populate;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.site.HtmlSection;
import bio.terra.pearl.core.model.site.HtmlSectionType;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.*;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.site.SiteContentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.survey.ResponseSnapshotService;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.populate.service.EnvironmentPopulator;
import bio.terra.pearl.populate.service.PortalPopulator;
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
    private ResponseSnapshotService responseSnapshotService;
    @Autowired
    private PortalEnvironmentService portalEnvironmentService;
    @Autowired
    private SiteContentService siteContentService;

    private void setUpEnvironments() throws IOException {
        for (EnvironmentName envName : EnvironmentName.values()) {
            environmentPopulator.populate("environments/" + envName + ".json");
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
        Portal portal = portalPopulator.populate("portals/ourhealth/portal.json");
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
    }

    private void checkOurhealthSurveys(Enrollee jonas) throws IOException {
        Survey medHistorySurvey = surveyService.findByStableId("oh_oh_medHx", 1).get();

        List<SurveyResponse> jonasResponses = surveyResponseService.findByEnrolleeId(jonas.getId());
        Assertions.assertEquals(1, jonasResponses.size());
        SurveyResponse medHistoryResp = jonasResponses.stream()
                .filter(response -> medHistorySurvey.getId().equals(response.getSurveyId()))
                .findFirst().get();
        ResponseSnapshot medHistorySnapshot = surveyResponseService.findOneWithLastSnapshot(medHistoryResp.getId())
                .get().getLastSnapshot();
        ParsedSnapshot parsedSnap = responseSnapshotService.parse(medHistorySnapshot);
        ResponseDataItem firstAnswer = parsedSnap.getParsedData().getItems().get(0);
        Assertions.assertEquals("yesSpecificallyAboutMyHeart", firstAnswer.getSimpleValue());
    }

    private void checkOurhealthSiteContent(UUID portalId) {
        PortalEnvironment portalEnv = portalEnvironmentService
                .loadWithParticipantSiteContent("ourhealth", EnvironmentName.sandbox, "en").get();
        Assertions.assertEquals(portalId, portalEnv.getPortalId());
        HtmlSection firstLandingSection = portalEnv.getSiteContent().getLocalizedSiteContents()
                .stream().findFirst().get().getLandingPage()
                .getSections().stream().findFirst().get();
        Assertions.assertEquals(HtmlSectionType.HERO_WITH_IMAGE, firstLandingSection.getSectionType());
    }

    @Test
    @Transactional
    public void testPopulateHeartHive() throws IOException {
        setUpEnvironments();
        Portal portal = portalPopulator.populate("portals/hearthive/portal.json");
        Assertions.assertEquals("hearthive", portal.getShortcode());
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
    }
}
