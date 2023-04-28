package bio.terra.pearl.populate;

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
import bio.terra.pearl.core.service.export.ExportFileFormat;
import bio.terra.pearl.core.service.export.instance.ExportOptions;
import bio.terra.pearl.core.service.export.instance.ModuleExportInfo;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

public class PopulateOurhealthTest extends BasePopulatePortalsTest {

    /**
     * populate the ourhealth study and do cursory checks on it.
     * This test will need to be updated as we change/update the prepopulated data.
     * @throws IOException
     */
    @Test
    @Transactional
    public void testPopulateOurHealth() throws Exception {
        setUpEnvironments();
        Portal portal = portalPopulator.populate(new FilePopulateContext("portals/ourhealth/portal.json"), true);
        Assertions.assertEquals("ourhealth", portal.getShortcode());

        Study mainStudy = portal.getPortalStudies().stream().findFirst().get().getStudy();
        Set<StudyEnvironment> studyEnvs = studyEnvironmentService.findByStudy(mainStudy.getId());
        Assertions.assertEquals(3, studyEnvs.size());
        UUID sandboxEnvironmentId = studyEnvs.stream().filter(
                        sEnv -> sEnv.getEnvironmentName().equals(EnvironmentName.sandbox))
                .findFirst().get().getId();

        List<Enrollee> enrollees = enrolleeService.findByStudyEnvironment(sandboxEnvironmentId);
        Assertions.assertEquals(3, enrollees.size());
        Enrollee jonas = enrollees.stream().filter(enrollee -> "OHSALK".equals(enrollee.getShortcode()))
                .findFirst().get();
        checkOurhealthSurveys(jonas);

        checkOurhealthSiteContent(portal.getId());
        checkExportContent(portal.getId(), sandboxEnvironmentId);
        checkDataDictionary(portal.getId(), sandboxEnvironmentId);

        // now check that we can populate it again, to make sure we don't have deletion issues
        portalPopulator.populate(new FilePopulateContext("portals/ourhealth/portal.json"), true);
    }

    private void checkOurhealthSurveys(Enrollee jonas) throws IOException {
        Survey cardioHistorySurvey = surveyService.findByStableId("oh_oh_cardioHx", 1).get();

        List<SurveyResponse> jonasResponses = surveyResponseService.findByEnrolleeId(jonas.getId());
        Assertions.assertEquals(2, jonasResponses.size());
        SurveyResponse cardioHistoryResp = jonasResponses.stream()
                .filter(response -> cardioHistorySurvey.getId().equals(response.getSurveyId()))
                .findFirst().get();
        SurveyResponse fetchedResponse = surveyResponseService.findOneWithAnswers(cardioHistoryResp.getId()).get();
        Answer heartHealthAns = fetchedResponse.getAnswers().stream()
                .filter(ans -> ans.getQuestionStableId().equals("oh_oh_cardioHx_worriedHeartHealth")).findFirst().get();
        Assertions.assertEquals("yesSpecificallyAboutMyHeart", heartHealthAns.getStringValue());
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

    private void checkExportContent(UUID portalId, UUID sandboxEnvironmentId) throws Exception {
        ExportOptions options = new ExportOptions(false, false, true, ExportFileFormat.TSV, null);
        List<ModuleExportInfo> moduleInfos = enrolleeExportService.generateModuleInfos(options, portalId, sandboxEnvironmentId);
        List<Map<String, String>> exportData = enrolleeExportService.generateExportMaps(portalId, sandboxEnvironmentId, moduleInfos, options.limit());

        assertThat(exportData, hasSize(3));
        Map<String, String> jsalkMap = exportData.stream().filter(map -> "OHSALK".equals(map.get("enrollee.shortcode")))
                .findFirst().get();
        assertThat(jsalkMap.get("profile.mailingAddress.street1"), equalTo("123 Walnut Street"));
        assertThat(jsalkMap.get("oh_oh_cardioHx.oh_oh_cardioHx_worriedHeartHealth"),
                equalTo("Yes, specifically about my heart"));
    }

    private void checkDataDictionary(UUID portalId, UUID sandboxEnvironmentId) throws Exception {
        ExportOptions options = new ExportOptions(false, false, true, ExportFileFormat.TSV, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        dictionaryExportService.exportDictionary(options, portalId, sandboxEnvironmentId, baos);
        baos.flush();
        baos.close();
        // the output is excel, so for now just check that any bytes were written
        assertThat(baos.size(), greaterThan(10));
    }

}
