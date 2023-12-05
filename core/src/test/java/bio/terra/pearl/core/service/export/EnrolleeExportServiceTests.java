package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;

import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.service.export.formatters.item.AnswerItemFormatter;
import bio.terra.pearl.core.service.export.formatters.item.ItemFormatter;
import bio.terra.pearl.core.service.export.formatters.item.PropertyItemFormatter;
import bio.terra.pearl.core.service.export.formatters.module.ModuleFormatter;
import bio.terra.pearl.core.service.survey.SurveyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


/**
 * Tests for the EnrolleeExportService.  Note that end-to-end tests for the export service are in the
 * Populate tests, as those provide more realistic testing of the export against simulated submitted data
 */
public class EnrolleeExportServiceTests extends BaseSpringBootTest {
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private EnrolleeExportService enrolleeExportService;
    @Autowired
    private SurveyService surveyService;
    @Autowired
    private SurveyFactory surveyFactory;

    @Test
    public void testExportNumberLimit(TestInfo testInfo) throws Exception {
        String testName = getTestName(testInfo);
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(testName);
        Enrollee enrollee1 = enrolleeFactory.buildPersisted(testName, studyEnv, new Profile());
        Enrollee enrollee2 = enrolleeFactory.buildPersisted(testName, studyEnv, new Profile());
        Enrollee enrollee3 = enrolleeFactory.buildPersisted(testName, studyEnv, new Profile());
        var exportModuleInfo = enrolleeExportService.generateModuleInfos(new ExportOptions(), studyEnv.getId());
        List<Map<String, String>> exportMaps = enrolleeExportService.generateExportMaps(studyEnv.getId(), exportModuleInfo, 2);

        assertThat(exportMaps, hasSize(2));
        // confirm enrollees are in reverse order of creation
        assertThat(exportMaps.get(0).get("enrollee.shortcode"), equalTo(enrollee3.getShortcode()));
        assertThat(exportMaps.get(1).get("enrollee.shortcode"), equalTo(enrollee2.getShortcode()));
    }

    private final String SOCIAL_HEALTH_EXCERPT = """
            {
              "pages": [
                  {
                    "elements": [             
                      {
                        "name": "hd_hd_socialHealth_neighborhoodSharesValues",
                        "type": "radiogroup",
                        "title": "People in my neighborhood share the same values as me.",
                        "choices": [
                          {"text": "Agree", "value": "agree"},
                          {"text": "Neutral (neither agree nor disagree)", "value": "neutralNeitherAgreeNorDisagree"},
                          {"text": "Disagree", "value": "disagree"}                      
                        ]
                      },
                      {
                        "name": "hd_hd_socialHealth_neighborhoodIsWalkable",
                        "type": "radiogroup",
                        "title": "My neighborhood is walkable.",
                        "choices": [
                          {"text": "Disagre", "value": "disagree"},
                          {"text": "Agree", "value": "agree"}
                        ]
                      }
                    ]
                  }
                 ]
               }
            """;

    @Test
    @Transactional
    public void testGenerateSurveyModules(TestInfo testInfo) throws Exception {
        String testName = getTestName(testInfo);
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(testName);
        Survey survey = surveyService.create(Survey.builder()
                .content(SOCIAL_HEALTH_EXCERPT)
                .name("Social Health")
                .stableId("socialHealth")
                .version(1)
                .build());
        surveyFactory.attachToEnv(survey, studyEnv.getId(), true);

        List<ModuleFormatter> moduleFormatters = enrolleeExportService.generateSurveyModules(new ExportOptions(), studyEnv.getId());
        assertThat(moduleFormatters, hasSize(1));
        ModuleFormatter<SurveyResponse, ItemFormatter<SurveyResponse>> socialHealthModule = moduleFormatters.get(0);
        assertThat(socialHealthModule.getModuleName(), equalTo(survey.getStableId()));
        assertThat(socialHealthModule.getItemFormatters(), hasSize(4));
        // module should contain both the response properties and question items
        assertThat(socialHealthModule.getItemFormatters().stream()
                .filter(itemFormatter -> itemFormatter instanceof PropertyItemFormatter)
                .map(itemFormatter -> ((PropertyItemFormatter) itemFormatter).getPropertyName()).toList(),
                hasItems("lastUpdatedAt", "complete"));
        assertThat(socialHealthModule.getItemFormatters().stream()
                        .filter(itemFormatter -> itemFormatter instanceof AnswerItemFormatter)
                        .map(itemFormatter -> ((AnswerItemFormatter) itemFormatter).getQuestionStableId()).toList(),
                hasItems("hd_hd_socialHealth_neighborhoodSharesValues", "hd_hd_socialHealth_neighborhoodIsWalkable"));
    }

    private final String SOCIAL_HEALTH_V2_EXCERPT = """
            {
              "pages": [
                  {
                    "elements": [             
                      {
                        "name": "hd_hd_socialHealth_neighborhoodNoisy",
                        "type": "radiogroup",
                        "title": "People in my neighborhood are noisy.",
                        "choices": [
                          {"text": "Agree", "value": "agree"},                    
                          {"text": "Disagre", "value": "disagree"}                      
                        ]
                      },
                      {
                        "name": "hd_hd_socialHealth_neighborhoodIsWalkable",
                        "type": "radiogroup",
                        "title": "My neighborhood is walkable.",
                        "choices": [
                          {"text": "Disagree", "value": "disagree"},
                          {"text": "Agree", "value": "agree"}
                        ]
                      }
                    ]
                  }
                 ]
               }
            """;

    @Test
    @Transactional
    public void testGenerateSurveyModulesAcrossVersions(TestInfo testInfo) throws Exception {
        String testName = getTestName(testInfo);
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(testName);
        Survey survey = surveyService.create(Survey.builder()
                .content(SOCIAL_HEALTH_EXCERPT)
                .name("Social Health")
                .stableId("socialHealth")
                .version(1)
                .build());
        surveyFactory.attachToEnv(survey, studyEnv.getId(), false);

        Survey survey2 = surveyService.create(Survey.builder()
                .content(SOCIAL_HEALTH_V2_EXCERPT)
                .name("Social Health")
                .stableId("socialHealth")
                .version(2)
                .build());
        surveyFactory.attachToEnv(survey2, studyEnv.getId(), true);

        var exportModuleInfo = enrolleeExportService.generateSurveyModules(new ExportOptions(), studyEnv.getId());
        assertThat(exportModuleInfo, hasSize(1));
        ModuleFormatter<SurveyResponse, ItemFormatter<SurveyResponse>> socialHealthModule = exportModuleInfo.get(0);
        assertThat(socialHealthModule.getModuleName(), equalTo(survey.getStableId()));
        // module should contain both question items from both surveys, but no duplicates
        assertThat(socialHealthModule.getItemFormatters(), hasSize(5));
        assertThat(socialHealthModule.getItemFormatters().stream()
                        .filter(itemFormatter -> itemFormatter instanceof AnswerItemFormatter)
                        .map(itemFormatter -> ((AnswerItemFormatter) itemFormatter).getQuestionStableId()).toList(),
                hasItems("hd_hd_socialHealth_neighborhoodSharesValues", "hd_hd_socialHealth_neighborhoodIsWalkable", "hd_hd_socialHealth_neighborhoodNoisy"));
    }
}
