package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.*;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.model.survey.SurveyType;
import bio.terra.pearl.core.service.export.formatters.item.AnswerItemFormatter;
import bio.terra.pearl.core.service.export.formatters.item.ItemFormatter;
import bio.terra.pearl.core.service.export.formatters.item.PropertyItemFormatter;
import bio.terra.pearl.core.service.export.formatters.module.ModuleFormatter;
import bio.terra.pearl.core.service.export.formatters.module.SurveyFormatter;
import bio.terra.pearl.core.service.participant.EnrolleeRelationService;
import bio.terra.pearl.core.service.participant.FamilyEnrolleeService;
import bio.terra.pearl.core.service.participant.FamilyService;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import bio.terra.pearl.core.service.study.StudyEnvironmentConfigService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.survey.SurveyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    @Autowired
    private EnrolleeSearchExpressionParser enrolleeSearchExpressionParser;
    @Autowired
    private FamilyService familyService;
    @Autowired
    private FamilyEnrolleeService familyEnrolleeService;
    @Autowired
    private StudyEnvironmentConfigService studyEnvironmentConfigService;
    @Autowired
    private StudyEnvironmentService studyEnvironmentService;
    @Autowired
    private EnrolleeRelationService enrolleeRelationService;

    @Test
    @Transactional
    public void testExportNumberLimit(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(testName);
        Enrollee enrollee1 = enrolleeFactory.buildPersisted(testName, studyEnv, new Profile());
        Enrollee enrollee2 = enrolleeFactory.buildPersisted(testName, studyEnv, new Profile());
        Enrollee enrollee3 = enrolleeFactory.buildPersisted(testName, studyEnv, new Profile());

        List<EnrolleeExportData> exportData = enrolleeExportService.loadEnrolleeExportData(studyEnv.getId(), new ExportOptions());
        List<ModuleFormatter> exportModuleInfo = enrolleeExportService.generateModuleInfos(new ExportOptions(), studyEnv.getId(), exportData);
        List<Map<String, String>> exportMaps = enrolleeExportService.generateExportMaps(exportData, exportModuleInfo);

        assertThat(exportMaps, hasSize(2));
        // confirm enrollees are in reverse order of creation
        assertThat(exportMaps.get(0).get("enrollee.shortcode"), equalTo(enrollee3.getShortcode()));
        assertThat(exportMaps.get(1).get("enrollee.shortcode"), equalTo(enrollee2.getShortcode()));
    }

    @Test
    @Transactional
    public void testExportWithProxies(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        StudyEnvironmentFactory.StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(testName, EnvironmentName.live);
        StudyEnvironment studyEnv = studyEnvBundle.getStudyEnv();
        EnrolleeFactory.EnrolleeAndProxy enrolleeWithProxy = enrolleeFactory.buildProxyAndGovernedEnrollee(testName, studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());
        Enrollee regularEnrollee = enrolleeFactory.buildPersisted(testName, studyEnv, new Profile());

        List<EnrolleeExportData> exportData = enrolleeExportService.loadEnrolleeExportData(studyEnv.getId(), new ExportOptions());
        List<ModuleFormatter> exportModuleInfoWithProxies = enrolleeExportService.generateModuleInfos(ExportOptions
                        .builder()
                        .filter(null) // no filter means proxies will be included
                        .onlyIncludeMostRecent(true)
                        .fileFormat(ExportFileFormat.TSV)
                        .limit(null)
                        .build(),
                studyEnv.getId(),
                exportData);

        List<Map<String, String>> exportMapsWithProxies = enrolleeExportService.generateExportMaps(List.of(), exportModuleInfoWithProxies);
        assertThat(exportMapsWithProxies, hasSize(3));

        assertThat(exportMapsWithProxies.get(0).get("enrollee.shortcode"), equalTo(regularEnrollee.getShortcode()));
        assertThat(exportMapsWithProxies.get(0).get("enrollee.subject"), equalTo("true"));

        assertThat(exportMapsWithProxies.get(1).get("enrollee.shortcode"), equalTo(enrolleeWithProxy.governedEnrollee().getShortcode()));
        assertThat(exportMapsWithProxies.get(1).get("enrollee.subject"), equalTo("true"));

        assertThat(exportMapsWithProxies.get(2).get("enrollee.shortcode"), equalTo(enrolleeWithProxy.proxy().getShortcode()));
        assertThat(exportMapsWithProxies.get(2).get("enrollee.subject"), equalTo("false"));


        List<EnrolleeExportData> exportDataNoProxies = enrolleeExportService.loadEnrolleeExportData(
                studyEnv.getId(),
                ExportOptions
                        .builder()
                        .filter(enrolleeSearchExpressionParser.parseRule("{enrollee.subject} = true"))
                        .build());
        List<ModuleFormatter> exportModuleInfoNoProxies = enrolleeExportService.generateModuleInfos(new ExportOptions(), studyEnv.getId(), exportDataNoProxies);
        List<Map<String, String>> exportMapsNoProxies = enrolleeExportService.generateExportMaps(exportData, exportModuleInfoNoProxies);

        assertThat(exportMapsNoProxies, hasSize(2));

        assertThat(exportMapsNoProxies.get(0).get("enrollee.shortcode"), equalTo(regularEnrollee.getShortcode()));
        assertThat(exportMapsNoProxies.get(1).get("enrollee.subject"), equalTo("true"));

        assertThat(exportMapsNoProxies.get(1).get("enrollee.shortcode"), equalTo(enrolleeWithProxy.governedEnrollee().getShortcode()));
        assertThat(exportMapsNoProxies.get(1).get("enrollee.subject"), equalTo("true"));
    }

    @Test
    @Transactional
    public void testExportChecksStudyEnvConfigProxy(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        StudyEnvironmentFactory.StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(testName, EnvironmentName.sandbox);
        StudyEnvironment studyEnv = studyEnvBundle.getStudyEnv();
        PortalEnvironment portalEnv = studyEnvBundle.getPortalEnv();

        enrolleeFactory.buildProxyAndGovernedEnrollee(testName, portalEnv, studyEnv);

        List<EnrolleeExportData> exportData = enrolleeExportService.loadEnrolleeExportData(studyEnv.getId(), new ExportOptions());
        List<ModuleFormatter> exportModuleInfo = enrolleeExportService.generateModuleInfos(new ExportOptions(), studyEnv.getId(), exportData);
        List<Map<String, String>> exportMaps = enrolleeExportService.generateExportMaps(exportData, exportModuleInfo);

        // no family or relation data should be exported because the study env config has neither enabled
        assertThat(exportMaps, hasSize(2));
        assertThat(exportMaps.get(0).containsKey("family.shortcode"), equalTo(false));
        assertThat(exportMaps.get(0).containsKey("relation.relationshipType"), equalTo(false));

        // enable proxy but not family
        studyEnvironmentConfigService.update(StudyEnvironmentConfig
                .builder()
                .id(studyEnv.getStudyEnvironmentConfigId())
                .acceptingProxyEnrollment(true)
                .build());


        exportModuleInfo = enrolleeExportService.generateModuleInfos(new ExportOptions(), studyEnv.getId(), exportData);
        exportMaps = enrolleeExportService.generateExportMaps(exportData, exportModuleInfo);

        // should export relation but not family data
        assertThat(exportMaps, hasSize(2));
        assertThat(exportMaps.get(0).get("relation.relationshipType"), equalTo("PROXY"));
        assertThat(exportMaps.get(0).containsKey("family.shortcode"), equalTo(false));
        assertThat(exportMaps.get(0).containsKey("family.shortcode"), equalTo(false));
    }

    @Test
    @Transactional
    public void testExportChecksStudyEnvConfigFamily(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(testName);
        Enrollee enrollee = enrolleeFactory.buildPersisted(testName, studyEnv, new Profile());
        Enrollee enrollee2 = enrolleeFactory.buildPersisted(testName, studyEnv, new Profile());

        UUID studyEnvId = enrollee.getStudyEnvironmentId();
        Family family = familyService.create(
                Family.builder().studyEnvironmentId(studyEnvId).probandEnrolleeId(enrollee.getId()).shortcode("FAMILY1").build(),
                getAuditInfo(testInfo));

        familyEnrolleeService.bulkCreate(
                List.of(
                        FamilyEnrollee.builder().enrolleeId(enrollee.getId()).familyId(family.getId()).build(),
                        FamilyEnrollee.builder().enrolleeId(enrollee2.getId()).familyId(family.getId()).build()
                ),
                getAuditInfo(testInfo)
        );

        enrolleeRelationService.create(
                EnrolleeRelation
                        .builder()
                        .familyRelationship("father")
                        .relationshipType(RelationshipType.FAMILY)
                        .familyId(family.getId())
                        .enrolleeId(enrollee.getId())
                        .targetEnrolleeId(enrollee2.getId())
                        .build(),
                getAuditInfo(testInfo)
        );

        List<ModuleFormatter> exportModuleInfo = enrolleeExportService.generateModuleInfos(new ExportOptions(), studyEnvId, List.of());
        List<Map<String, String>> exportMaps = enrolleeExportService.generateExportMaps(List.of(), exportModuleInfo);

        // no family or relation data should be exported because the study env config has neither enabled
        assertThat(exportMaps, hasSize(2));
        assertThat(exportMaps.get(0).containsKey("family.shortcode"), equalTo(false));
        assertThat(exportMaps.get(0).containsKey("relation.relationshipType"), equalTo(false));

        // enable family data
        StudyEnvironmentConfig config = studyEnvironmentConfigService.findByStudyEnvironmentId(studyEnvId);
        studyEnvironmentConfigService.update(StudyEnvironmentConfig
                .builder()
                .id(config.getId())
                .enableFamilyLinkage(true)
                .build());

        exportModuleInfo = enrolleeExportService.generateModuleInfos(new ExportOptions(), studyEnvId, List.of());
        exportMaps = enrolleeExportService.generateExportMaps(List.of(), exportModuleInfo);

        // should export family and relation data
        assertThat(exportMaps, hasSize(2));
        assertThat(exportMaps.get(0).get("family.shortcode"), equalTo(family.getShortcode()));
        assertThat(exportMaps.get(1).get("family.shortcode"), equalTo(family.getShortcode()));

        Map<String, String> targetExportMap = exportMaps.stream().filter(map -> map.get("enrollee.shortcode").equals(enrollee2.getShortcode())).findFirst().get();
        assertThat(targetExportMap.get("relation.relationshipType"), equalTo("FAMILY"));
        assertThat(targetExportMap.get("relation.familyRelationship"), equalTo("father"));
        assertThat(targetExportMap.get("relation.family.shortcode"), equalTo("FAMILY1"));

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
        Survey survey = surveyService.create(
                surveyFactory
                        .builderWithDependencies(getTestName(testInfo))
                        .content(SOCIAL_HEALTH_EXCERPT)
                        .name("Social Health")
                        .stableId("socialHealth")
                        .surveyType(SurveyType.RESEARCH)
                        .version(1)
                        .build());
        surveyFactory.attachToEnv(survey, studyEnv.getId(), true);


        List<ModuleFormatter> moduleFormatters = enrolleeExportService.generateModuleInfos(new ExportOptions(), studyEnv.getId(), List.of());

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
        StudyEnvironmentFactory.StudyEnvironmentBundle bundle = studyEnvironmentFactory.buildBundle(testName, EnvironmentName.sandbox);
        StudyEnvironment studyEnv = bundle.getStudyEnv();

        Survey survey = surveyService.create(
                surveyFactory
                        .builder(getTestName(testInfo))
                        .portalId(bundle.getPortal().getId())
                        .content(SOCIAL_HEALTH_EXCERPT)
                        .name("Social Health")
                        .stableId("socialHealth")
                        .surveyType(SurveyType.RESEARCH)
                        .version(1)
                        .build());
        surveyFactory.attachToEnv(survey, studyEnv.getId(), false);

        Survey survey2 = surveyService.create(
                surveyFactory
                        .builder(getTestName(testInfo))
                        .portalId(bundle.getPortal().getId())
                        .content(SOCIAL_HEALTH_V2_EXCERPT)
                        .name("Social Health")
                        .stableId("socialHealth")
                        .surveyType(SurveyType.RESEARCH)
                        .version(2)
                        .build());
        surveyFactory.attachToEnv(survey2, studyEnv.getId(), true);

        List<SurveyFormatter> exportModuleInfo = enrolleeExportService.generateSurveyModules(new ExportOptions(), studyEnv.getId(), List.of());
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
