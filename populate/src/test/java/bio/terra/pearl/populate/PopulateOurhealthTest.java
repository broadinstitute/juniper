package bio.terra.pearl.populate;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantNote;
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
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.ExportFileFormat;
import bio.terra.pearl.core.service.export.ExportOptions;
import bio.terra.pearl.core.service.export.formatters.module.ModuleFormatter;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PopulateOurhealthTest extends BasePopulatePortalsTest {

    /**
     * populate the ourhealth study and do cursory checks on it.
     * This test will need to be updated as we change/update the prepopulated data.
     * @throws IOException
     */
    @Test
    @Transactional
    public void testPopulateOurHealth() throws Exception {
        Portal portal = portalPopulator.populate(new FilePopulateContext("portals/ourhealth/portal.json"), true);
        Assertions.assertEquals("ourhealth", portal.getShortcode());

        Study mainStudy = portal.getPortalStudies().stream().findFirst().get().getStudy();
        List<StudyEnvironment> studyEnvs = studyEnvironmentService.findByStudy(mainStudy.getId());
        Assertions.assertEquals(3, studyEnvs.size());
        UUID sandboxEnvironmentId = studyEnvs.stream().filter(
                        sEnv -> sEnv.getEnvironmentName().equals(EnvironmentName.sandbox))
                .findFirst().get().getId();

        List<Enrollee> enrollees = enrolleeService.findByStudyEnvironment(sandboxEnvironmentId);
        Assertions.assertEquals(6, enrollees.size());
        checkOurhealthSurveys(enrollees, portal.getId());
        checkParticipantNotes(enrollees);
        checkAdminTasks(sandboxEnvironmentId);
        checkOurhealthSiteContent(portal.getId());
        checkExportContent(sandboxEnvironmentId);
        checkDataDictionary(portal.getId(), sandboxEnvironmentId);
        checkWithdrawn();

        // now check that we can populate it again, to make sure we don't have deletion issues
        portalPopulator.populate(new FilePopulateContext("portals/ourhealth/portal.json"), true);
    }

    private void checkOurhealthSurveys(List<Enrollee> enrollees, UUID portalId) {
        Enrollee jonas = getJonasSalk(enrollees);
        Survey cardioHistorySurvey = surveyService.findByStableId("oh_oh_cardioHx", 1, portalId).get();

        List<SurveyResponse> jonasResponses = surveyResponseService.findByEnrolleeId(jonas.getId());
        Assertions.assertEquals(5, jonasResponses.size());
        SurveyResponse cardioHistoryResp = jonasResponses.stream()
                .filter(response -> cardioHistorySurvey.getId().equals(response.getSurveyId()))
                .findFirst().get();
        SurveyResponse fetchedResponse = surveyResponseService.findOneWithAnswers(cardioHistoryResp.getId()).get();
        Answer heartHealthAns = fetchedResponse.getAnswers().stream()
                .filter(ans -> ans.getQuestionStableId().equals("oh_oh_cardioHx_worriedHeartHealth")).findFirst().get();
        Assertions.assertEquals("yes", heartHealthAns.getStringValue());
    }

    private void checkOurhealthSiteContent(UUID portalId) {
        PortalEnvironment portalEnv = portalEnvironmentService
                .loadWithParticipantSiteContent("ourhealth", EnvironmentName.sandbox, null).get();
        Assertions.assertEquals(portalId, portalEnv.getPortalId());
        LocalizedSiteContent lsc = portalEnv.getSiteContent().getLocalizedSiteContents()
                .stream().findFirst().get();
        HtmlSection firstLandingSection = lsc.getLandingPage()
                .getSections().stream().findFirst().get();
        Assertions.assertEquals(HtmlSectionType.HERO_WITH_IMAGE, firstLandingSection.getSectionType());
        Assertions.assertNotNull(lsc.getFooterSection());
    }

    private void checkParticipantNotes(List<Enrollee> enrollees) {
        Enrollee jonas = getJonasSalk(enrollees);
        List<ParticipantNote> notes = participantNoteService.findByEnrollee(jonas.getId());
        assertThat(notes, hasSize(3));
        List<ParticipantNote> kitNotes = notes.stream().filter(note -> note.getKitRequestId() != null).toList();
        assertThat(kitNotes, hasSize(1));
        assertThat(kitNotes.get(0).getText(), equalTo("Phone call: asked to delay kit shipment as they are travelling for next two months"));
    }

    private void checkAdminTasks(UUID sandboxStudyEnvId) {
        ParticipantTaskService.ParticipantTaskTaskListDto taskInfo = participantTaskService.findAdminTasksByStudyEnvironmentId(sandboxStudyEnvId,
                List.of("participantNote", "enrollee"));
        // check that 3 tasks from 2 enrollees, related to 3 notes
        assertThat(taskInfo.tasks(), hasSize(3));
        assertThat(taskInfo.enrollees(), hasSize(2));
        assertThat(taskInfo.participantNotes(), hasSize(3));
    }

    private void checkExportContent(UUID sandboxEnvironmentId) {
        // test the analysis-friendly export as that is the most important for data integrity, and the least visible via admin tool
        ExportOptions options = ExportOptions
                .builder()
                .splitOptionsIntoColumns(true)
                .stableIdsForOptions(true)
                .onlyIncludeMostRecent(true)
                .fileFormat(ExportFileFormat.TSV)
                .limit(null)
                .build();
        List<EnrolleeExportData> enrolleeExportData = enrolleeExportService.loadEnrolleeExportData(sandboxEnvironmentId, options);
        List<ModuleFormatter> moduleInfos = enrolleeExportService.generateModuleInfos(options, sandboxEnvironmentId, enrolleeExportData);
        List<Map<String, String>> exportData = enrolleeExportService.generateExportMaps(enrolleeExportData, moduleInfos);

        assertThat(exportData, hasSize(6));
        Map<String, String> jsalkMap = exportData.stream().filter(map -> "OHSALK".equals(map.get("enrollee.shortcode")))
                .findFirst().get();
        assertThat(jsalkMap.get("profile.mailingAddress.street1"), equalTo("415 Main Street"));
        assertThat(jsalkMap.get("oh_oh_cardioHx.oh_oh_cardioHx_worriedHeartHealth"),
                equalTo("yes"));
        assertThat(jsalkMap.get("oh_oh_cardioHx.oh_oh_cardioHx_diagnosedHeartConditions.bleedingDisorder"), equalTo("1"));
        assertThat(jsalkMap.get("oh_oh_cardioHx.oh_oh_cardioHx_diagnosedHeartConditions.anemia"), equalTo("1"));
        assertThat(jsalkMap.get("oh_oh_cardioHx.oh_oh_cardioHx_diagnosedHeartConditions.cardiacArrest"), equalTo(null));
    }

    private void checkDataDictionary(UUID portalId, UUID sandboxEnvironmentId) throws Exception {
        ExportOptions options = ExportOptions
                .builder()
                .onlyIncludeMostRecent(true)
                .fileFormat(ExportFileFormat.TSV)
                .limit(null)
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        dictionaryExportService.exportDictionary(options, portalId, sandboxEnvironmentId, baos);
        baos.flush();
        baos.close();
        // the output is excel, so for now just check that any bytes were written
        assertThat(baos.size(), greaterThan(10));
    }

    private void checkWithdrawn() throws Exception {
        assertThat(withdrawnEnrolleeService.isWithdrawn("OHGONE"), is(true));
        assertThat(enrolleeService.findOneByShortcode("OHGONE").isEmpty(), is(true));
        assertThat(withdrawnEnrolleeService.isWithdrawn("OHSALK"), is(false));
    }

    private Enrollee getJonasSalk(List<Enrollee> enrollees) {
        return enrollees.stream().filter(enrollee -> "OHSALK".equals(enrollee.getShortcode()))
            .findFirst().get();
    }

}
