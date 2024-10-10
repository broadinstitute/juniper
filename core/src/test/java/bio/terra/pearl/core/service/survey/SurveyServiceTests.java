package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.i18n.LanguageTextDao;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.StudyEnvironmentBundle;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.i18n.LanguageText;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.survey.*;
import bio.terra.pearl.core.service.CascadeProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.test.util.AssertionErrors.fail;

public class SurveyServiceTests extends BaseSpringBootTest {
    @Autowired
    private SurveyService surveyService;
    @Autowired
    private SurveyFactory surveyFactory;
    @Autowired
    private AdminUserFactory adminUserFactory;
    @Autowired
    private PortalFactory portalFactory;
    @Autowired
    private LanguageTextDao languageTextDao;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;

    @Mock
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Transactional
    public void testCreateSurvey(TestInfo info) {
        Portal portal = portalFactory.buildPersisted(getTestName(info));

        Survey survey = surveyFactory.builder(getTestName(info)).build();
        survey.setPortalId(portal.getId());
        survey.setCreatedAt(null);
        survey.setLastUpdatedAt(null);
        Survey savedSurvey = surveyService.create(survey);
        DaoTestUtils.assertGeneratedProperties(savedSurvey);
        Assertions.assertEquals(savedSurvey.getName(), survey.getName());

        Survey fetchedSurvey = surveyService.findByStableId(savedSurvey.getStableId(), savedSurvey.getVersion(), savedSurvey.getPortalId()).get();
        Assertions.assertEquals(fetchedSurvey.getId(), savedSurvey.getId());
    }

    @Test
    @Transactional
    public void testFindNoContent(TestInfo info) {
        Survey survey = surveyFactory.builderWithDependencies(getTestName(info)).build();
        survey.setSurveyType(SurveyType.OUTREACH);
        survey.setAutoAssign(true);
        UUID portalId = surveyService.create(survey).getPortalId();

        Survey fetchedSurvey = surveyService.findByStableId(survey.getStableId(), portalId).get(0);
        Survey fetchedNoContentSurvey = surveyService.findByStableIdNoContent(survey.getStableId()).get(0);
        assertThat(fetchedSurvey, samePropertyValuesAs(fetchedNoContentSurvey, "content"));
        assertThat(fetchedNoContentSurvey.getContent(), nullValue());
    }

    @Test
    @Transactional
    public void testCreateSurveyWithMappings(TestInfo info) {
        Survey survey = surveyFactory.builderWithDependencies(getTestName(info)).build();
        AnswerMapping answerMapping = AnswerMapping.builder()
                .questionStableId("qStableId")
                .targetField("givenName")
                .mapType(AnswerMappingMapType.STRING_TO_STRING)
                .targetType(AnswerMappingTargetType.PROFILE)
                .build();
        survey.getAnswerMappings().add(answerMapping);
        Survey savedSurvey = surveyService.create(survey);
        assertThat(savedSurvey.getAnswerMappings(), hasSize(1));
        DaoTestUtils.assertGeneratedProperties(savedSurvey.getAnswerMappings().get(0));

        Survey fetchedSurvey = surveyService.findByStableIdWithMappings(savedSurvey.getStableId(), savedSurvey.getVersion(), savedSurvey.getPortalId()).get();
        assertThat(fetchedSurvey.getAnswerMappings(), hasSize(1));
    }

    @Test
    @Transactional
    public void testCreateNewVersion(TestInfo info) {
        Survey survey = surveyFactory.buildPersisted(getTestName(info));
        String oldContent = survey.getContent();
        String newContent = String.format("{\"pages\":[],\"title\":\"%s\"}", RandomStringUtils.randomAlphabetic(6));
        survey.setContent(newContent);
        Survey newSurvey = surveyService.createNewVersion(survey.getPortalId(), survey);

        Assertions.assertNotEquals(newSurvey.getId(), survey.getId());
        // check version was incremented and content was modified
        Assertions.assertEquals(survey.getVersion() + 1, newSurvey.getVersion());
        Assertions.assertEquals(newContent, newSurvey.getContent());

        // confirm the existing survey wasn't modified
        Survey fetchedOriginal = surveyService.find(survey.getId()).get();
        Assertions.assertEquals(oldContent, fetchedOriginal.getContent());
    }

    @Test
    @Transactional
    public void testAssignPublishedVersion(TestInfo info) {
        Survey survey = surveyFactory.buildPersisted(getTestName(info));
        surveyService.assignPublishedVersion(survey.getId());
        survey = surveyService.find(survey.getId()).get();
        assertThat(survey.getPublishedVersion(), equalTo(1));

        String newContent = String.format("{\"pages\":[],\"title\":\"%s\"}", RandomStringUtils.randomAlphabetic(6));
        survey.setContent(newContent);
        Survey newSurvey = surveyService.createNewVersion(survey.getPortalId(), survey);

        Assertions.assertNotEquals(newSurvey.getId(), survey.getId());
        // check published version was NOT copied
        assertThat(newSurvey.getPublishedVersion(), equalTo(null));

        surveyService.assignPublishedVersion(newSurvey.getId());
        newSurvey = surveyService.find(newSurvey.getId()).get();
        assertThat(newSurvey.getPublishedVersion(), equalTo(2));
    }


    @Test
    @Transactional
    public void testCreateNewVersionWithMappings(TestInfo info) {
        AnswerMapping answerMapping = AnswerMapping.builder()
                .questionStableId("qStableId")
                .targetField("givenName")
                .mapType(AnswerMappingMapType.STRING_TO_STRING)
                .targetType(AnswerMappingTargetType.PROFILE)
                .build();
        Survey survey = surveyFactory.buildPersisted(getTestName(info), List.of(answerMapping));
        AdminUser user = adminUserFactory.buildPersisted(getTestName(info));
        String oldContent = survey.getContent();
        String newContent = String.format("{\"pages\":[],\"title\":\"%s\"}", RandomStringUtils.randomAlphabetic(6));
        survey.setContent(newContent);

        AnswerMapping answerMappingUpdate = AnswerMapping.builder()
                .questionStableId("qStableId")
                .targetField("lastName")
                .mapType(AnswerMappingMapType.STRING_TO_STRING)
                .targetType(AnswerMappingTargetType.PROFILE)
                .build();
        survey.getAnswerMappings().clear();
        survey.getAnswerMappings().add(answerMappingUpdate);
        Survey newSurvey = surveyService.createNewVersion(survey.getPortalId(), survey);
        surveyService.attachAnswerMappings(newSurvey);
        // check version was incremented and answer mappings were saved too
        Assertions.assertEquals(survey.getVersion() + 1, newSurvey.getVersion());
        Assertions.assertEquals(1, newSurvey.getAnswerMappings().size());
        Assertions.assertEquals("lastName", newSurvey.getAnswerMappings().get(0).getTargetField());

        // confirm old version answer mappings weren't changed
        surveyService.attachAnswerMappings(survey);
        Assertions.assertEquals(1, survey.getAnswerMappings().size());
        Assertions.assertEquals("givenName", survey.getAnswerMappings().get(0).getTargetField());

    }

    @Test
    @Transactional
    public void testGetSurveyQuestionDefinitions(TestInfo info) {
        Survey survey = surveyFactory.buildPersisted(getTestName(info));

        survey.setContent(twoQuestionSurveyContent);

        List<SurveyQuestionDefinition> actual = surveyService.getSurveyQuestionDefinitions(survey);
        Assertions.assertEquals(4, actual.size());
    }

    @Test
    @Transactional
    public void testCreateSurveyQuestionDefinitionsSameShortcode(TestInfo info) {
        String sharedStableId = "someSurvey" + RandomStringUtils.randomAlphabetic(4);
        Survey survey = surveyFactory.builderWithDependencies(getTestName(info))
                .stableId(sharedStableId)
                .content(twoQuestionSurveyContent)
                        .build();
        surveyService.create(survey);

        Survey survey2 = surveyFactory.builderWithDependencies(getTestName(info))
                .stableId(sharedStableId)
                .content(twoQuestionSurveyContent)
                .build();
        surveyService.create(survey2);

        List<SurveyQuestionDefinition> actual = surveyService.getSurveyQuestionDefinitions(survey);
        Assertions.assertEquals(4, actual.size());
        List<SurveyQuestionDefinition> actual2 = surveyService.getSurveyQuestionDefinitions(survey2);
        Assertions.assertEquals(4, actual2.size());
    }

    @Test
    @Transactional
    public void testCreateSurveyQuestionDefinitionsWhitespace(TestInfo info) {
        Survey survey = surveyFactory.builderWithDependencies(getTestName(info))
                .content("""
                        {              	      
                            "pages": [{
                            "elements": [{
                                "name": "oh_oh_basic firstName",
                                "type": "text",
                                "title": "First name",
                                "isRequired": true
                            }""")
                .build();
        Assertions.assertThrows(IllegalArgumentException.class, () -> surveyService.create(survey));
    }

    private final String twoQuestionSurveyContent = """
                {
                	"title": "The Basics",
                	"showQuestionNumbers": "off",
                	"pages": [{
                		"elements": [{
                			"name": "oh_oh_basic_firstName",
                			"type": "text",
                			"title": "First name",
                			"isRequired": true
                		}, {
                			"name": "oh_oh_basic_mghPatient",
                			"type": "radiogroup",
                			"title": "Are you a patient, current or former, of MGH?",
                			"isRequired": true,
                			"choices": [{
                				"text": "Yes",
                				"value": "yes"
                			}, {
                				"text": "No",
                				"value": "no"
                			}]
                		}]
                	}, {
                		"elements": [{
                			"name": "oh_oh_basic_streetAddress",
                			"type": "text",
                			"title": "Address",
                			"isRequired": true
                		}, {
                			"name": "oh_oh_basic_city",
                			"type": "text",
                			"title": "City",
                			"isRequired": true
                		}]
                	}]
                }""";


    @Test
    @Transactional
    public void testProcessDerivedQuestions() {
        Survey survey = new Survey();
        String surveyContent = """
                {
                  "title": "The Basics",
                  "pages": [{
                    "name": "page1",
                    "elements": [{
                      "type": "text",
                      "name": "oh_oh_basic_heightUnit",
                      "title": "Height unit (cm or in)"
                     },{
                      "type": "text",
                      "name": "oh_oh_basic_rawHeight",
                      "title": "Height"
                     },{
                      "type": "text",
                      "name": "oh_oh_basic_weightUnit",
                      "title": "Weight unit (lbs or kg)"
                     },{
                      "type": "text",
                      "name": "oh_oh_basic_rawWeight",
                      "title": "Weight"
                     }]
                   }],
                  "calculatedValues": [{
                    "name": "computedHeight",
                    "includeIntoResult": true,
                    "expression": "iif({oh_oh_basic_heightUnit} = 'in', {oh_oh_basic_rawHeight} * 2.54, {oh_oh_basic_rawHeight}"
                   }]
                 }""";

        survey.setContent(surveyContent);
        List<SurveyQuestionDefinition> defs = surveyService.getSurveyQuestionDefinitions(survey);
        assertThat(defs, hasSize(5));
        // check that the question got inserted after the first one it is derived from
        assertThat(defs.get(2).getQuestionStableId(), equalTo("computedHeight"));
        assertThat(defs.get(2).getQuestionType(), equalTo(SurveyParseUtils.DERIVED_QUESTION_TYPE));
    }

    @Test
    void testGetAnswerByStableId_HappyPath()  {
        objectMapper = new ObjectMapper();
        String surveyJsonData = "[{\"createdAt\":1710527621.051735000,\"lastUpdatedAt\":1710527621.051735000,\"questionStableId\":"
                + "\"q1\",\"surveyVersion\":0,\"viewedLanguage\":\"en\",\"stringValue\":\"answer\"}]";
        String questionStableId = "q1";
        Class<String> returnClass = String.class;
        String result = SurveyParseUtils.getAnswerByStableId(surveyJsonData, questionStableId, returnClass, objectMapper, "stringValue");
        assertEquals("answer", result);
    }

    @Test
    void testGetQuestionStableId_WhenFieldExists() throws Exception {
        objectMapper = new ObjectMapper();
        String json = "{\"questionStableId\": \"expectedId\"}";
        JsonNode rootNode = objectMapper.readTree(json);
        String result = SurveyParseUtils.getQuestionStableId(rootNode);

        assertEquals("expectedId", result);
    }

    @Test
    void testGetQuestionStableId_WhenFieldDoesNotExist() {
        objectMapper = new ObjectMapper();
        String json = "{\"someOtherField\": \"value\"}";
        try {
            assertNull(SurveyParseUtils.getQuestionStableId(objectMapper.readTree(json)));
        } catch (Exception e) {
            fail("Failed to parse JSON");
        }
    }

    @Test
    void testConvertNodeToClass() throws JsonProcessingException {
        objectMapper = new ObjectMapper();
        String json = "{\"objectValue\": \"[\\\"answer1\\\", \\\"answer2\\\"]\"}";
        JsonNode node = objectMapper.readTree(json);
        String result = SurveyParseUtils.convertQuestionAnswerToClass(node, "objectValue", String.class, objectMapper);
        assertEquals("[\"answer1\", \"answer2\"]", result);

        String json2 = "{\"objectValue\": \"true\"}";
        node = objectMapper.readTree(json2);
        Boolean booleanResult = SurveyParseUtils.convertQuestionAnswerToClass(node, "objectValue", Boolean.class, objectMapper);
        assertEquals(true, booleanResult);

        String json3 = "{\"objectValue\": \"3\"}";
        node = objectMapper.readTree(json3);
        int integerResult = SurveyParseUtils.convertQuestionAnswerToClass(node, "objectValue", Integer.class, objectMapper);
        assertEquals(3, integerResult);
    }

    @Test
    void testGetSurveyAnswerFromPreEnrollSurveyJsonData() {
        String jsonInput = """
                [{"createdAt":1710527621.050828000,"lastUpdatedAt":1710527621.050828000,"questionStableId":
                "hd_hd_preenroll_southAsianAncestry","surveyVersion":0,"viewedLanguage":"en","stringValue":"yes"},
                {"createdAt":1710527621.050843000,"lastUpdatedAt":1710527621.050843000,"questionStableId":
                "hd_hd_preenroll_understandsEnglish","surveyVersion":0,"viewedLanguage":"en","stringValue":"yes"},
                {"createdAt":1710527621.050851000,"lastUpdatedAt":1710527621.050851000,"questionStableId":
                "hd_hd_preenroll_isAdult","surveyVersion":0,"viewedLanguage":"en","stringValue":"yes"},
                {"createdAt":1710527621.051394000,"lastUpdatedAt":1710527621.051394000,"questionStableId":
                "hd_hd_preenroll_livesInUS","surveyVersion":0,"viewedLanguage":"en","stringValue":"yes"},
                {"createdAt":1710527621.051735000,"lastUpdatedAt":1710527621.051735000,"questionStableId":
                "proxy_enrollment","surveyVersion":0,"viewedLanguage":"en","stringValue":"true"},
                {"createdAt":1710527621.051748000,"lastUpdatedAt":1710527621.051748000,"questionStableId":
                "qualified","surveyVersion":0,"viewedLanguage":"en","booleanValue":true}]""";

        Boolean result = SurveyParseUtils.getAnswerByStableId(jsonInput, "proxy_enrollment", Boolean.class, new ObjectMapper(), "stringValue");
        assertEquals(true, result);
    }

    @Test
    @Transactional
    void createStripsWhitespace(TestInfo info) {

        Portal portal = portalFactory.buildPersisted(getTestName(info));

        Survey survey = Survey
                .builder()
                .stableId("   \t\t   survey_1      ")
                .content("""
                        {
                          "title": "The Basics",
                          "pages": []
                        }
                        """)
                .portalId(portal.getId())
                .eligibilityRule("")
                .build();

        Survey savedSurvey = surveyService.create(survey);

        assertEquals("survey_1", savedSurvey.getStableId());
    }

    @Test
    @Transactional
    public void testCreateDeleteWithLanguageTexts(TestInfo info) {
        Portal portal = portalFactory.buildPersisted(getTestName(info));
        Survey survey = Survey
                .builder()
                .stableId("survey" + getTestName(info))
                .content("""
                        {
                          "title": {
                            "default": "The Basics",
                            "es": "Los Basicos",
                            "dev": "DEV_The Basics"
                          },
                          "showQuestionNumbers": "off",
                          "showProgressBar": "bottom",
                          "pages": []
                        }""")
                .portalId(portal.getId())
                .eligibilityRule("")
                .build();
        survey = surveyService.create(survey);
        List<LanguageText> languageTexts = languageTextDao.findByPortalId(portal.getId(), "es");
        assertThat(languageTexts, hasSize(1));
        assertThat(languageTexts.get(0).getKeyName(), equalTo(survey.getStableId() + ":1"));
        surveyService.delete(survey.getId(), CascadeProperty.EMPTY_SET);

        assertThat(languageTextDao.findByPortalId(portal.getId(), "es"), hasSize(0));
    }

    @Test
    @Transactional
    void testCreateSurveyWithDynamicPanel(TestInfo info) {

        Portal portal = portalFactory.buildPersisted(getTestName(info));

        Survey survey = Survey
                .builder()
                .stableId("exampleSurvey")
                .content("""
                        {
                          "title": "The Basics",
                          "pages": [{
                                "name": "page1",
                                "elements": [{
                                  "type": "paneldynamic",
                                  "name": "oh_oh_languages",
                                  "title": "What languages do you speak at home?",
                                  "templateElements": [
                                    {
                                      "type": "text",
                                      "name": "language",
                                      "title": "Language",
                                      "isRequired": true
                                    },
                                    {
                                      "type": "dropdown",
                                      "name": "proficiency",
                                      "title": "Proficiency",
                                      "isRequired": true,
                                      "choices": [
                                        {"value": "native", "text": "Native"},
                                        {"value": "fluent", "text": "Fluent"},
                                        {"value": "proficient", "text": "Proficient"},
                                        {"value": "basic", "text": "Basic"}
                                      ]
                                    }
                                  ]
                                 }
                                ]
                           }]
                        }
                        """)
                .portalId(portal.getId())
                .eligibilityRule("")
                .build();

        Survey savedSurvey = surveyService.create(survey);

        List<SurveyQuestionDefinition> questionDefinitions = surveyService.getSurveyQuestionDefinitions(savedSurvey);

        assertThat(questionDefinitions, hasSize(3));

        SurveyQuestionDefinition dynamicPanel = questionDefinitions.stream().filter(q -> q.getQuestionStableId().equals("oh_oh_languages")).findFirst().get();
        assertThat(dynamicPanel.getQuestionStableId(), equalTo("oh_oh_languages"));
        assertThat(dynamicPanel.getQuestionType(), equalTo("paneldynamic"));
        assertThat(dynamicPanel.isRepeatable(), equalTo(true));

        SurveyQuestionDefinition language = questionDefinitions.stream().filter(q -> q.getQuestionStableId().equals("language")).findFirst().get();

        assertThat(language.getQuestionStableId(), equalTo("language"));
        assertThat(language.getQuestionType(), equalTo("text"));
        assertThat(language.getParentStableId(), equalTo("oh_oh_languages"));

        SurveyQuestionDefinition proficiency = questionDefinitions.stream().filter(q -> q.getQuestionStableId().equals("proficiency")).findFirst().get();

        assertThat(proficiency.getQuestionStableId(), equalTo("proficiency"));
        assertThat(proficiency.getQuestionType(), equalTo("dropdown"));
        assertThat(proficiency.getParentStableId(), equalTo("oh_oh_languages"));
        assertThat(proficiency.getChoices(), equalTo("""
                [{"stableId":"native","text":"Native"},{"stableId":"fluent","text":"Fluent"},{"stableId":"proficient","text":"Proficient"},{"stableId":"basic","text":"Basic"}]"""));
    }

    @Test
    @Transactional
    public void testSurveyReferencesAnswers(TestInfo info) {
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);

        Survey survey = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(info))
                .portalId(studyEnvBundle.getPortal().getId())
                .stableId("survey1")
                .content("{\"pages\":[{\"elements\":[{\"type\":\"text\",\"name\":\"diagnosis\",\"title\":\"Testing {another_survey.something_else}\"}]}]}"));


        Assertions.assertEquals(1, survey.getReferencedQuestions().size());
        Assertions.assertEquals("another_survey.something_else", survey.getReferencedQuestions().getFirst());
    }

    @Test
    @Transactional
    public void testSurveyReferencesAnswerFiltersObjectQuestions(TestInfo info) {
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);

        Survey survey = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(info))
                .portalId(studyEnvBundle.getPortal().getId())
                .stableId("survey1")
                .content("{\"pages\":[{\"elements\":[{\"type\":\"text\",\"name\":\"testing\",\"title\":\"Testing {some_complex_question.data}\"},{\"type\":\"text\",\"name\":\"some_complex_question\",\"title\":\"Testing\"}]}]}"));


        Assertions.assertEquals(0, survey.getReferencedQuestions().size());

    }

}
