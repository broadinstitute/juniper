package bio.terra.pearl.populate;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyType;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

/** confirm the RGP portal populates as expected */
public class PopulateRgpTest extends BasePopulatePortalsTest {
    @Test
    @Transactional
    public void testPopulateRgp() throws Exception {
        baseSeedPopulator.populateRolesAndPermissions();
        Portal portal = portalPopulator.populate(new FilePopulateContext("portals/rgp/portal.json"), true);
        Assertions.assertEquals("rgp", portal.getShortcode());
        assertThat(portal.getPortalStudies(), hasSize(1));

        Study study = studyService.findByShortcode("rgp").orElseThrow();
        StudyEnvironment sandbox = studyEnvironmentService.findByStudy(study.getShortcode(), EnvironmentName.sandbox).orElseThrow();

        // the pepper prequal is the pre-enroll survey, with the "qualified" calculated value gating enrollment
        Survey preEnroll = surveyService.find(sandbox.getPreEnrollSurveyId()).orElseThrow();
        Assertions.assertEquals(SurveyType.PRE_ENROLL, preEnroll.getSurveyType());
        Assertions.assertEquals(1, preEnroll.getVersion());
        Assertions.assertTrue(preEnroll.getContent().contains("\"qualified\""));
        // the admin "Forms & Surveys" page lists pre-enroll surveys from the configured surveys, not from preEnrollSurveyId
        assertThat(surveyService.findActiveInStudyEnvWithType(sandbox.getId(), SurveyType.PRE_ENROLL)
                .stream().map(Survey::getStableId).toList(), containsInAnyOrder("prequal"));

        // the family-member surveys from pepper are questions within enrollment, not separate surveys
        Survey enrollment = surveyService.findByStableIdAndPortalShortcodeWithMappings("enrollment", 1, "rgp").orElseThrow();
        Assertions.assertTrue(enrollment.getContent().contains("\"fathersRace\""));
        Assertions.assertTrue(enrollment.getContent().contains("\"siblings\""));

        // staff-facing forms, matching the DSM participant info / study status / identifier fields
        List<String> adminSurveys = surveyService.findActiveInStudyEnvWithType(sandbox.getId(), SurveyType.ADMIN)
                .stream().map(Survey::getStableId).toList();
        assertThat(adminSurveys, containsInAnyOrder("participantInfo", "studyStatus", "legacyIdentifiers"));
    }
}
