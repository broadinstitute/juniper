package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentBundle;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeAndProxy;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.survey.AnswerFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.factory.survey.SurveyResponseFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.export.ExportOptions;
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
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import bio.terra.pearl.core.service.study.StudyEnvironmentConfigService;
import bio.terra.pearl.core.service.survey.SurveyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;


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
    private EnrolleeRelationService enrolleeRelationService;
    @Autowired
    private ParticipantUserService participantUserService;
    @Autowired
    private AnswerFactory answerFactory;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SurveyResponseFactory surveyResponseFactory;

    @Test
    @Transactional
    public void testExportNumberLimit(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(testName);
        Enrollee enrollee1 = enrolleeFactory.buildPersisted(testName, studyEnv, new Profile());
        Enrollee enrollee2 = enrolleeFactory.buildPersisted(testName, studyEnv, new Profile());
        Enrollee enrollee3 = enrolleeFactory.buildPersisted(testName, studyEnv, new Profile());

        ExportOptionsWithExpression opts = ExportOptionsWithExpression.builder().rowLimit(2).build();

        List<EnrolleeExportData> exportData = enrolleeExportService.loadEnrolleeExportData(studyEnv.getId(), opts);
        List<ModuleFormatter> exportModuleInfo = enrolleeExportService.generateModuleInfos(opts, studyEnv.getId(), exportData);
        List<Map<String, String>> exportMaps = enrolleeExportService.generateExportMaps(exportData, exportModuleInfo);

        assertThat(exportMaps, hasSize(2));
        // confirm enrollees are in reverse order of creation
        assertThat(exportMaps.get(0).get("enrollee.shortcode"), equalTo(enrollee3.getShortcode()));
        assertThat(exportMaps.get(1).get("enrollee.shortcode"), equalTo(enrollee2.getShortcode()));
    }

    @Test
    @Transactional
    public void testExportIncludeFields(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(testName);
        enrolleeFactory.buildPersisted(testName, studyEnv, new Profile());

        ExportOptionsWithExpression opts = ExportOptionsWithExpression.builder()
                .fileFormat(ExportFileFormat.TSV)
                .includeFields(List.of("enrollee.shortcode", "profile.familyName")).build();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        enrolleeExportService.export(opts, studyEnv.getId(), stream);

        assertThat(stream.toString(), startsWith("enrollee.shortcode\tprofile.familyName\nShortcode"));
    }

    @Test
    @Transactional
    public void testExportIncludeFieldsSorted(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(testName, EnvironmentName.irb);
        EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(testName, studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());

        ExportOptionsWithExpression opts = ExportOptionsWithExpression.builder()
                .fileFormat(ExportFileFormat.TSV)
                .includeFields(List.of("profile.familyName", "enrollee.shortcode", "account.username" )).build();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        enrolleeExportService.export(opts, studyEnvBundle.getStudyEnv().getId(), stream);

        assertThat(stream.toString(), startsWith("profile.familyName\tenrollee.shortcode\taccount.username\nFamily Name"));
        // fun fact -- empty strings are typically left blank, but TSV export will quote the first column for safety if it's empty.
        assertThat(stream.toString(), endsWith("\n\"\"\t%s\t%s\n".formatted(enrolleeBundle.enrollee().getShortcode(), enrolleeBundle.participantUser().getUsername())));

        opts = ExportOptionsWithExpression.builder()
                .fileFormat(ExportFileFormat.TSV)
                .includeFields(List.of("account.username", "profile.familyName", "enrollee.shortcode")).build();
        stream = new ByteArrayOutputStream();
        enrolleeExportService.export(opts, studyEnvBundle.getStudyEnv().getId(), stream);

        assertThat(stream.toString(), startsWith("account.username\tprofile.familyName\tenrollee.shortcode\nUsername"));
        assertThat(stream.toString(), endsWith("\n%s\t\t%s\n".formatted(enrolleeBundle.participantUser().getUsername(), enrolleeBundle.enrollee().getShortcode())));
    }

    @Test
    @Transactional
    public void testExportStudyShortcode(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        StudyEnvironmentBundle bundle = studyEnvironmentFactory.buildBundle(testName, EnvironmentName.sandbox);
        Enrollee enrollee1 = enrolleeFactory.buildPersisted(testName, bundle.getStudyEnv(), new Profile());

        ExportOptionsWithExpression opts = ExportOptionsWithExpression.builder().rowLimit(2).build();

        List<EnrolleeExportData> exportData = enrolleeExportService.loadEnrolleeExportData(bundle.getStudyEnv().getId(), opts);
        List<ModuleFormatter> exportModuleInfo = enrolleeExportService.generateModuleInfos(opts, bundle.getStudyEnv().getId(), exportData);
        List<Map<String, String>> exportMaps = enrolleeExportService.generateExportMaps(exportData, exportModuleInfo);

        // confirm enrollees are in reverse order of creation
        assertThat(exportMaps.get(0).get("study.shortcode"), equalTo(bundle.getStudy().getShortcode()));
    }

    @Test
    @Transactional
    public void testExportWithProxies(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(testName, EnvironmentName.live);
        StudyEnvironment studyEnv = studyEnvBundle.getStudyEnv();

        StudyEnvironmentConfig config = studyEnvironmentConfigService.find(studyEnv.getStudyEnvironmentConfigId()).get();
        config.setAcceptingProxyEnrollment(true);
        studyEnvironmentConfigService.update(config);

        EnrolleeAndProxy enrolleeWithProxy = enrolleeFactory.buildProxyAndGovernedEnrollee(testName, studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());
        Enrollee regularEnrollee = enrolleeFactory.buildPersisted(testName, studyEnv, new Profile());

        ParticipantUser proxyUser = participantUserService.findByEnrolleeId(enrolleeWithProxy.proxy().getId()).get();

        List<EnrolleeExportData> exportData = enrolleeExportService.loadEnrolleeExportData(studyEnv.getId(), new ExportOptionsWithExpression());
        List<ModuleFormatter> exportModuleInfoWithProxies = enrolleeExportService.generateModuleInfos(ExportOptions
                        .builder()
                        .filterString(null) // no filter means proxies will be included
                        .onlyIncludeMostRecent(true)
                        .fileFormat(ExportFileFormat.TSV)
                        .rowLimit(null)
                        .build(),
                studyEnv.getId(),
                exportData);

        List<Map<String, String>> exportMapsWithProxies = enrolleeExportService.generateExportMaps(exportData, exportModuleInfoWithProxies);
        assertThat(exportMapsWithProxies, hasSize(3));

        assertThat(exportMapsWithProxies.get(0).get("enrollee.shortcode"), equalTo(regularEnrollee.getShortcode()));
        assertThat(exportMapsWithProxies.get(0).get("enrollee.subject"), equalTo("true"));
        assertThat(exportMapsWithProxies.get(1).get("proxy.username"), equalTo(proxyUser.getUsername()));

        assertThat(exportMapsWithProxies.get(1).get("enrollee.shortcode"), equalTo(enrolleeWithProxy.governedEnrollee().getShortcode()));
        assertThat(exportMapsWithProxies.get(1).get("enrollee.subject"), equalTo("true"));
        assertThat(exportMapsWithProxies.get(1).get("proxy.username"), equalTo(proxyUser.getUsername()));

        assertThat(exportMapsWithProxies.get(2).get("enrollee.shortcode"), equalTo(enrolleeWithProxy.proxy().getShortcode()));
        assertThat(exportMapsWithProxies.get(2).get("enrollee.subject"), equalTo("false"));
        assertThat(exportMapsWithProxies.get(2).get("proxy.username"), equalTo(null));

        List<EnrolleeExportData> exportDataNoProxies = enrolleeExportService.loadEnrolleeExportData(
                studyEnv.getId(),
                ExportOptionsWithExpression
                        .builder()
                        .filterExpression(enrolleeSearchExpressionParser.parseRule("{enrollee.subject} = true"))
                        .build());
        List<ModuleFormatter> exportModuleInfoNoProxies = enrolleeExportService.generateModuleInfos(new ExportOptions(), studyEnv.getId(), exportDataNoProxies);
        List<Map<String, String>> exportMapsNoProxies = enrolleeExportService.generateExportMaps(exportDataNoProxies, exportModuleInfoNoProxies);

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
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(testName, EnvironmentName.sandbox);
        StudyEnvironment studyEnv = studyEnvBundle.getStudyEnv();
        PortalEnvironment portalEnv = studyEnvBundle.getPortalEnv();

        enrolleeFactory.buildProxyAndGovernedEnrollee(testName, portalEnv, studyEnv);

        List<EnrolleeExportData> exportData = enrolleeExportService.loadEnrolleeExportData(studyEnv.getId(), new ExportOptionsWithExpression());
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

        exportData = enrolleeExportService.loadEnrolleeExportData(studyEnv.getId(), new ExportOptionsWithExpression());
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

        List<EnrolleeExportData> data = enrolleeExportService.loadEnrolleeExportData(studyEnvId, new ExportOptionsWithExpression());
        List<ModuleFormatter> exportModuleInfo = enrolleeExportService.generateModuleInfos(new ExportOptions(), studyEnvId, data);
        List<Map<String, String>> exportMaps = enrolleeExportService.generateExportMaps(data, exportModuleInfo);

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

        data = enrolleeExportService.loadEnrolleeExportData(studyEnvId, new ExportOptionsWithExpression());
        exportModuleInfo = enrolleeExportService.generateModuleInfos(new ExportOptions(), studyEnvId, data);
        exportMaps = enrolleeExportService.generateExportMaps(data, exportModuleInfo);

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


        List<SurveyFormatter> moduleFormatters = enrolleeExportService.generateSurveyModules(new ExportOptions(), studyEnv.getId(), List.of());

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
        StudyEnvironmentBundle bundle = studyEnvironmentFactory.buildBundle(testName, EnvironmentName.sandbox);
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


    private final String DYNAMIC_PANEL_EXCERPT = """
            {
              "pages": [
                  {
                    "elements": [             
                      {
                        "name": "examplePanel",
                        "type": "paneldynamic",
                        "title": "Names of people in your family",
                        "templateElements": [
                            {
                                "name": "firstName",
                                "type": "text",
                                "title": "First name",
                                "isRequired": true
                            },
                            {
                                "name": "lastName",
                                "type": "text",
                                "title": "Last name",
                                "isRequired": true
                            }
                        ]
                      }   
                    ]
                  }
                 ]
               }
            """;

    private final String DYNAMIC_PANEL_EXCERPT_NO_LASTNAME = """
            {
              "pages": [
                  {
                    "elements": [             
                      {
                        "name": "examplePanel",
                        "type": "paneldynamic",
                        "title": "Names of people in your family",
                        "templateElements": [
                            {
                                "name": "firstName",
                                "type": "text",
                                "title": "First name",
                                "isRequired": true
                            }
                        ]
                      }   
                    ]
                  }
                 ]
               }
            """;

    @Test
    @Transactional
    public void testDynamicPanelExport(TestInfo testInfo) throws Exception {
        String testName = getTestName(testInfo);
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(testName);
        Survey survey = surveyService.create(
                surveyFactory
                        .builderWithDependencies(getTestName(testInfo))
                        .content(DYNAMIC_PANEL_EXCERPT)
                        .name("Dynamic Panel Test")
                        .stableId("examplesurvey")
                        .surveyType(SurveyType.RESEARCH)
                        .version(1)
                        .build());

        surveyFactory.attachToEnv(survey, studyEnv.getId(), true);

        // 4 responses
        Enrollee enrollee1 = enrolleeFactory.buildPersisted(testName, studyEnv, new Profile());
        // 2 responses
        Enrollee enrollee2 = enrolleeFactory.buildPersisted(testName, studyEnv, new Profile());

        // make sure it doesn't blow up if no survey response / no answers
        Enrollee enrollee3 = enrolleeFactory.buildPersisted(testName, studyEnv, new Profile());
        enrolleeFactory.buildPersisted(testName, studyEnv, new Profile());

        surveyResponseFactory.buildWithAnswers(
                enrollee1,
                survey,
                Map.of(
                        "examplePanel", objectMapper.readTree("""
                                    [{"firstName":"John","lastName":"Doe"},
                                     {"firstName":"Jane","lastName":"Doe"},
                                     {"firstName":"Jim","lastName":"Doe"},
                                     {"firstName":"Jill","lastName":"Doe"}]
                                """)
                )
        );
        surveyResponseFactory.buildWithAnswers(
                enrollee2,
                survey,
                Map.of(
                        "examplePanel", objectMapper.readTree("""
                                    [{"firstName":"Jonas","lastName":"Salk"},
                                     {"firstName":"Peter","lastName":"Salk"}]
                                """)
                )
        );
        surveyResponseFactory.buildWithAnswers(
                enrollee3,
                survey,
                Map.of()
        );


        List<EnrolleeExportData> exportData = enrolleeExportService.loadEnrolleeExportData(studyEnv.getId(), new ExportOptionsWithExpression());
        List<ModuleFormatter> moduleFormatters = enrolleeExportService.generateModuleInfos(new ExportOptions(), studyEnv.getId(), exportData);
        List<Map<String, String>> exportMaps = enrolleeExportService.generateExportMaps(exportData, moduleFormatters);


        assertThat(exportMaps, hasSize(4));

        Map<String, String> enrollee1Map = exportMaps.stream().filter(map -> map.get("enrollee.shortcode").equals(enrollee1.getShortcode())).findFirst().get();
        Map<String, String> enrollee2Map = exportMaps.stream().filter(map -> map.get("enrollee.shortcode").equals(enrollee2.getShortcode())).findFirst().get();

        assertThat(enrollee1Map.get("examplesurvey.examplePanel.firstName[0]"), equalTo("John"));
        assertThat(enrollee1Map.get("examplesurvey.examplePanel.lastName[0]"), equalTo("Doe"));
        assertThat(enrollee1Map.get("examplesurvey.examplePanel.firstName[1]"), equalTo("Jane"));
        assertThat(enrollee1Map.get("examplesurvey.examplePanel.lastName[1]"), equalTo("Doe"));
        assertThat(enrollee1Map.get("examplesurvey.examplePanel.firstName[2]"), equalTo("Jim"));
        assertThat(enrollee1Map.get("examplesurvey.examplePanel.lastName[2]"), equalTo("Doe"));
        assertThat(enrollee1Map.get("examplesurvey.examplePanel.firstName[3]"), equalTo("Jill"));
        assertThat(enrollee1Map.get("examplesurvey.examplePanel.lastName[3]"), equalTo("Doe"));

        assertThat(enrollee2Map.get("examplesurvey.examplePanel.firstName[0]"), equalTo("Jonas"));
        assertThat(enrollee2Map.get("examplesurvey.examplePanel.lastName[0]"), equalTo("Salk"));
        assertThat(enrollee2Map.get("examplesurvey.examplePanel.firstName[1]"), equalTo("Peter"));
        assertThat(enrollee2Map.get("examplesurvey.examplePanel.lastName[1]"), equalTo("Salk"));
    }


    @Test
    @Transactional
    public void testDynamicPanelExportNoResponses(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(testName);
        Survey survey = surveyService.create(
                surveyFactory
                        .builderWithDependencies(getTestName(testInfo))
                        .content(DYNAMIC_PANEL_EXCERPT)
                        .name("Dynamic Panel Test")
                        .stableId("examplesurvey")
                        .surveyType(SurveyType.RESEARCH)
                        .version(1)
                        .build());

        surveyFactory.attachToEnv(survey, studyEnv.getId(), true);

        List<EnrolleeExportData> exportData = enrolleeExportService.loadEnrolleeExportData(studyEnv.getId(), new ExportOptionsWithExpression());
        List<ModuleFormatter> moduleFormatters = enrolleeExportService.generateModuleInfos(new ExportOptions(), studyEnv.getId(), exportData);
        List<Map<String, String>> exportMaps = enrolleeExportService.generateExportMaps(exportData, moduleFormatters);

        BaseExporter exporter = enrolleeExportService.getExporter(ExportFileFormat.CSV, moduleFormatters, exportMaps, null);
        List<String> columnKeys = exporter.getColumnKeys();
        List<String> columnHeaders = exporter.getHeaderRow();

        assertTrue(columnKeys.contains("examplesurvey.examplePanel.firstName[0]"));
        assertTrue(columnKeys.contains("examplesurvey.examplePanel.lastName[0]"));

        assertTrue(columnHeaders.contains("examplesurvey.examplePanel.firstName[0]"));
    }

    @Test
    @Transactional
    public void testMultiVersionDynamicPanelExport(TestInfo testInfo) throws Exception {
        String testName = getTestName(testInfo);
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(testName);
        Survey surveyV1 = surveyService.create(
                surveyFactory
                        .builderWithDependencies(getTestName(testInfo))
                        .content(DYNAMIC_PANEL_EXCERPT)
                        .name("Dynamic Panel Test")
                        .stableId("examplesurvey")
                        .surveyType(SurveyType.RESEARCH)
                        .version(1)
                        .build());

        surveyFactory.attachToEnv(surveyV1, studyEnv.getId(), false);

        Survey surveyV2 = surveyService.create(
                surveyFactory
                        .builderWithDependencies(getTestName(testInfo))
                        .content(DYNAMIC_PANEL_EXCERPT_NO_LASTNAME)
                        .name("Dynamic Panel Test")
                        .stableId("examplesurvey")
                        .surveyType(SurveyType.RESEARCH)
                        .version(1)
                        .build());

        surveyFactory.attachToEnv(surveyV2, studyEnv.getId(), true);

        Enrollee enrollee = enrolleeFactory.buildPersisted(testName, studyEnv, new Profile());

        // enrollee responded to old version of the survey
        surveyResponseFactory.buildWithAnswers(
                enrollee,
                surveyV1,
                Map.of(
                        "examplePanel", objectMapper.readTree("""
                                    [{"firstName":"John","lastName":"Doe"},
                                     {"firstName":"Jane","lastName":"Doe"},
                                     {"firstName":"Jim","lastName":"Doe"},
                                     {"firstName":"Jill","lastName":"Doe"}]
                                """)
                )
        );

        List<EnrolleeExportData> exportData = enrolleeExportService.loadEnrolleeExportData(studyEnv.getId(), new ExportOptionsWithExpression());
        List<ModuleFormatter> moduleFormatters = enrolleeExportService.generateModuleInfos(new ExportOptions(), studyEnv.getId(), exportData);
        List<Map<String, String>> exportMaps = enrolleeExportService.generateExportMaps(exportData, moduleFormatters);


        assertThat(exportMaps, hasSize(1));

        Map<String, String> enrolleeMap = exportMaps.get(0);

        // should still export survey responses for the old version of the survey
        assertThat(enrolleeMap.get("examplesurvey.examplePanel.firstName[0]"), equalTo("John"));
        assertThat(enrolleeMap.get("examplesurvey.examplePanel.lastName[0]"), equalTo("Doe"));
    }

    @Test
    @Transactional
    public void testExportSubheadersOption(TestInfo testInfo) throws Exception {
        String testName = getTestName(testInfo);
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(testName);
        Enrollee enrollee1 = enrolleeFactory.buildPersisted(testName, studyEnv, new Profile());

        ExportOptionsWithExpression opts = ExportOptionsWithExpression.builder()
                .fileFormat(ExportFileFormat.CSV)
                .includeSubHeaders(false).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        enrolleeExportService.export(opts, studyEnv.getId(), baos);
        baos.close();
        String export = baos.toString();
        assertThat(export, containsString(",enrollee.createdAt"));
        assertThat(export, not(containsString(",Created at")));

        // now check it includes subheaders if asked
        opts = ExportOptionsWithExpression.builder()
                .includeSubHeaders(true)
                .fileFormat(ExportFileFormat.CSV).build();
        baos = new ByteArrayOutputStream();
        enrolleeExportService.export(opts, studyEnv.getId(), baos);
        baos.close();
        export = baos.toString();
        assertThat(export, containsString(",enrollee.createdAt"));
        assertThat(export, containsString(",Created At"));
    }
}
