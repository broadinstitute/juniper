package bio.terra.pearl.populate;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.participant.RelationshipType;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.export.ExportFileFormat;
import bio.terra.pearl.core.service.export.ExportOptions;
import bio.terra.pearl.core.service.export.formatters.module.ModuleFormatter;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

/** confirm demo portal populates as expected */
public class PopulateDemoTest extends BasePopulatePortalsTest {
    @Test
    @Transactional
    public void testPopulateDemo() throws Exception {
        Portal portal = portalPopulator.populate(new FilePopulateContext("portals/demo/portal.json"), true);
        Assertions.assertEquals("demo", portal.getShortcode());
        PortalEnvironment sandbox = portalEnvironmentService.findOne("demo", EnvironmentName.sandbox).get();
        assertThat(portal.getPortalStudies(), hasSize(1));
        Study mainStudy = portal.getPortalStudies().stream().findFirst().get().getStudy();
        List<StudyEnvironment> studyEnvs = studyEnvironmentService.findByStudy(mainStudy.getId());
        Assertions.assertEquals(3, studyEnvs.size());
        UUID sandboxEnvironmentId = studyEnvs.stream().filter(
                        sEnv -> sEnv.getEnvironmentName().equals(EnvironmentName.sandbox))
                .findFirst().get().getId();

        List<Enrollee> enrollees = enrolleeService.findByStudyEnvironment(sandboxEnvironmentId);
        Assertions.assertEquals(11, enrollees.size());

        checkOldVersionEnrollee(enrollees);
        checkKeyedEnrollee(enrollees);
        checkProxyWithOneGovernedEnrollee(enrollees);
        checkProxyWithTwoGovernedEnrollee(enrollees);
        checkExportContent(sandboxEnvironmentId);
    }

    /** confirm the enrollee wtih answers to two different survey versions was populated correctly */
    private void checkOldVersionEnrollee(List<Enrollee> sandboxEnrollees) {
        Enrollee enrollee = sandboxEnrollees.stream().filter(sandboxEnrollee -> "HDVERS".equals(sandboxEnrollee.getShortcode()))
                .findFirst().get();

        List<Answer> socialHealthAnswers = answerService.findByEnrolleeAndSurvey(enrollee.getId(), "hd_hd_socialHealth");
        assertThat(socialHealthAnswers, hasSize(4));
        assertThat(socialHealthAnswers, hasItem(
                Matchers.both(hasProperty("questionStableId", equalTo("hd_hd_socialHealth_neighborhoodIsWalkable")))

                        .and(hasProperty("surveyVersion", equalTo(1))))
        );
        assertThat(socialHealthAnswers, hasItem(
                Matchers.both(hasProperty("questionStableId", equalTo("hd_hd_socialHealth_neighborhoodIsNoisy")))
                        .and(hasProperty("surveyVersion", equalTo(2))))
        );
    }

    private void checkKeyedEnrollee(List<Enrollee> sandboxEnrollees) {
        Enrollee enrollee = sandboxEnrollees.stream().filter(sandboxEnrollee -> "HDINVI".equals(sandboxEnrollee.getShortcode()))
                .findFirst().get();
        ParticipantUser user = participantUserService.find(enrollee.getParticipantUserId()).get();
        assertThat(user.getUsername().contains("+invited-"), equalTo(true));
        assertThat(user.getUsername(), endsWith("broadinstitute.org"));
    }

    /** confirm the proxy enrollee with one governed user was enrolled appropriately */
    private void checkProxyWithOneGovernedEnrollee(List<Enrollee> sandboxEnrollees) {
        Enrollee governedEnrollee = sandboxEnrollees.stream().filter(sandboxEnrollee -> "HDGOVR".equals(sandboxEnrollee.getShortcode()))
                .findFirst().get();
        // now check the EnrolleeRelation was created correctly
        Enrollee proxyEnrollee = sandboxEnrollees.stream().filter(sandboxEnrollee -> "HDPROX".equals(sandboxEnrollee.getShortcode()))
                .findFirst().get();
        assertThat(enrolleeRelationService.findByTargetEnrolleeId(governedEnrollee.getId()), hasSize(1));
        assertThat(enrolleeRelationService.findByTargetEnrolleeId(governedEnrollee.getId()).get(0).getEnrolleeId(), equalTo(proxyEnrollee.getId()));

        // now confirm PortalParticipantUsers were wired correctly
        List<PortalParticipantUser> portalParticipantUsers = portalParticipantUserService.findByParticipantUserId(proxyEnrollee.getParticipantUserId());
        assertThat(portalParticipantUsers, hasSize(1));
        assertThat(portalParticipantUsers.get(0).getParticipantUserId(), equalTo(proxyEnrollee.getParticipantUserId()));
        List<PortalParticipantUser> governedPortalParticipantUsers = portalParticipantUserService.findByParticipantUserId(governedEnrollee.getParticipantUserId());
        // now confirm profiles were setup correctly
        assertThat(governedPortalParticipantUsers, hasSize(1));
        assertThat(governedPortalParticipantUsers.get(0).getParticipantUserId(), equalTo(governedEnrollee.getParticipantUserId()));
        PortalParticipantUser proxyPPUser = portalParticipantUsers.get(0);
        PortalParticipantUser governedPPUser = governedPortalParticipantUsers.get(0);
        Profile proxyProfile = profileService.find(proxyPPUser.getProfileId()).get();
        Profile governedUserProfile = profileService.find(governedPPUser.getProfileId()).get();
        assertThat(proxyProfile.getContactEmail(), equalTo(governedUserProfile.getContactEmail()));
        assertThat(proxyProfile, not(equalTo(governedUserProfile)));
    }

    /** confirm the proxy enrollee with two governed users was enrolled appropriately */
    private void checkProxyWithTwoGovernedEnrollee(List<Enrollee> sandboxEnrollees) {
        Enrollee governedEnrollee1 = sandboxEnrollees.stream().filter(sandboxEnrollee -> "HDGVAA".equals(sandboxEnrollee.getShortcode()))
                .findFirst().get();
        Enrollee governedEnrollee2 = sandboxEnrollees.stream().filter(sandboxEnrollee -> "HDGVBA".equals(sandboxEnrollee.getShortcode()))
                .findFirst().get();
        Enrollee proxyEnrollee = sandboxEnrollees.stream().filter(sandboxEnrollee -> "HDPRXA".equals(sandboxEnrollee.getShortcode()))
                .findFirst().get();
        assertThat(enrolleeRelationService.findByEnrolleeIdAndRelationType(proxyEnrollee.getId(), RelationshipType.PROXY), hasSize(2));
        assertThat(enrolleeRelationService.findByEnrolleeIdAndRelationType(proxyEnrollee.getId(), RelationshipType.PROXY).stream().filter(enrolleeRelation -> enrolleeRelation.getTargetEnrolleeId().equals(governedEnrollee1.getId())).collect(
                Collectors.toList()), hasSize(1));
        assertThat(enrolleeRelationService.findByEnrolleeIdAndRelationType(proxyEnrollee.getId(), RelationshipType.PROXY).stream().filter(enrolleeRelation -> enrolleeRelation.getTargetEnrolleeId().equals(governedEnrollee2.getId())).collect(
                Collectors.toList()), hasSize(1));

        assertThat(enrolleeRelationService.findByTargetEnrolleeId(governedEnrollee1.getId()), hasSize(1));
        assertThat(enrolleeRelationService.findByTargetEnrolleeId(governedEnrollee1.getId()).get(0).getEnrolleeId(), equalTo(proxyEnrollee.getId()));
        assertThat(enrolleeRelationService.findByTargetEnrolleeId(governedEnrollee2.getId()), hasSize(1));
        assertThat(enrolleeRelationService.findByTargetEnrolleeId(governedEnrollee2.getId()).get(0).getEnrolleeId(), equalTo(proxyEnrollee.getId()));

        // now confirm PortalParticipantUsers were wired correctly
        List<PortalParticipantUser> proxyPortalParticipantUsers = portalParticipantUserService.findByParticipantUserId(proxyEnrollee.getParticipantUserId());
        assertThat(proxyPortalParticipantUsers, hasSize(1));
        assertThat(proxyPortalParticipantUsers.get(0).getParticipantUserId(), equalTo(proxyEnrollee.getParticipantUserId()));

        PortalParticipantUser governedPPUser1 = portalParticipantUserService.findByParticipantUserId(governedEnrollee1.getParticipantUserId()).get(0);
        PortalParticipantUser governedPPUser2 = portalParticipantUserService.findByParticipantUserId(governedEnrollee2.getParticipantUserId()).get(0);
        PortalParticipantUser proxyPPUser = proxyPortalParticipantUsers.get(0);
        Profile proxyProfile = profileService.find(proxyPPUser.getProfileId()).get();
        Profile governedUserProfile1 = profileService.find(governedPPUser1.getProfileId()).get();
        Profile governedUserProfile2 = profileService.find(governedPPUser2.getProfileId()).get();
        assertThat(proxyProfile.getContactEmail(), equalTo(governedUserProfile1.getContactEmail()));
        assertThat(proxyProfile.getContactEmail(), equalTo(governedUserProfile2.getContactEmail()));
        assertThat(proxyProfile, not(equalTo(governedUserProfile1)));
        assertThat(governedUserProfile2, not(equalTo(proxyProfile)));
        assertThat(governedUserProfile2, not(equalTo(governedUserProfile1)));
    }

    private void checkExportContent(UUID sandboxEnvironmentId) throws Exception {
        ExportOptions options = ExportOptions
                .builder()
                .onlyIncludeMostRecent(true)
                .fileFormat(ExportFileFormat.TSV)
                .limit(null)
                .build();
        List<ModuleFormatter> moduleInfos = enrolleeExportService.generateModuleInfos(options, sandboxEnvironmentId);
        List<Map<String, String>> exportData = enrolleeExportService.generateExportMaps(sandboxEnvironmentId, moduleInfos, false, options.getLimit());

        assertThat(exportData, hasSize(9));
        Map<String, String> oldVersionMap = exportData.stream().filter(map -> "HDVERS".equals(map.get("enrollee.shortcode")))
                .findFirst().get();
        assertThat(oldVersionMap.get("account.username"), equalTo("oldversion@test.com"));
        // confirm text (including typo) from prior version is carried through
        assertThat(oldVersionMap.get("hd_hd_socialHealth.hd_hd_socialHealth_neighborhoodSharesValues"), equalTo("Disagre"));
        // confirm answer from question that was removed in current version is still exported
        assertThat(oldVersionMap.get("hd_hd_socialHealth.hd_hd_socialHealth_neighborhoodIsWalkable"), equalTo("Disagree"));
    }

    @Test
    @Transactional
    public void testPopulateWithShortcodeOverride() {
        String newShortcode = RandomStringUtils.randomAlphabetic(6);
        Portal portal = portalPopulator.populate(new FilePopulateContext("portals/demo/portal.json", false, newShortcode), true);
        assertThat(portal.getShortcode(), equalTo(newShortcode));
        Study mainStudy = portal.getPortalStudies().stream().findFirst().get().getStudy();
        assertThat(mainStudy.getShortcode(), equalTo(newShortcode + "_heartdemo"));
        List<Survey> surveys = surveyService.findByPortalId(portal.getId());
        assertThat(surveys, hasSize(13));
        surveys.forEach(survey -> {
            assertThat(survey.getStableId(), Matchers.startsWith(newShortcode));
        });
        List<ConsentForm> consentForms = consentFormService.findByPortalId(portal.getId());
        assertThat(consentForms, hasSize(0));
        consentForms.forEach(consentForm -> {
            assertThat(consentForm.getStableId(), Matchers.startsWith(newShortcode));
        });
        List<SiteContent> siteContents = siteContentService.findByPortalId(portal.getId());
        assertThat(siteContents, hasSize(2));
        siteContents.forEach(siteContent -> {
            assertThat(siteContent.getStableId(), Matchers.startsWith(newShortcode));
        });
        List<EmailTemplate> emailTemplates = emailTemplateService.findByPortalId(portal.getId());
        assertThat(emailTemplates, hasSize(6));
        emailTemplates.forEach(emailTemplate -> {
            assertThat(emailTemplate.getStableId(), Matchers.startsWith(newShortcode));
        });
    }
}
