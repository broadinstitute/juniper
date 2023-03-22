package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.DaoTestUtils;
import bio.terra.pearl.core.factory.AdminUserFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.survey.*;
import bio.terra.pearl.core.util.SurveyUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class SurveyServiceTests extends BaseSpringBootTest {
    @Autowired
    private SurveyService surveyService;
    @Autowired
    private SurveyFactory surveyFactory;
    @Autowired
    private AdminUserFactory adminUserFactory;

    @Test
    @Transactional
    public void testCreateSurvey() {
        Survey survey = surveyFactory.builder("testPublishSurvey").build();
        Survey savedSurvey = surveyService.create(survey);
        DaoTestUtils.assertGeneratedProperties(savedSurvey);
        Assertions.assertEquals(savedSurvey.getName(), survey.getName());

        Survey fetchedSurvey = surveyService.findByStableId(savedSurvey.getStableId(), savedSurvey.getVersion()).get();
        Assertions.assertEquals(fetchedSurvey.getId(), savedSurvey.getId());
    }

    @Test
    @Transactional
    public void testCreateSurveyWithMappings() {
        Survey survey = surveyFactory.builder("testPublishSurvey").build();
        AnswerMapping answerMapping = AnswerMapping.builder()
                .questionStableId("qStableId")
                .targetField("givenName")
                .mapType(AnswerMappingMapType.TEXT_NODE_TO_STRING)
                .targetType(AnswerMappingTargetType.PROFILE)
                .build();
        survey.getAnswerMappings().add(answerMapping);
        Survey savedSurvey = surveyService.create(survey);
        assertThat(savedSurvey.getAnswerMappings(), hasSize(1));
        DaoTestUtils.assertGeneratedProperties(savedSurvey.getAnswerMappings().get(0));

        Survey fetchedSurvey = surveyService.findByStableIdWithMappings(savedSurvey.getStableId(), savedSurvey.getVersion()).get();
        assertThat(fetchedSurvey.getAnswerMappings(), hasSize(1));
    }

    @Test
    @Transactional
    public void testCreateNewVersion() {
        Survey survey = surveyFactory.buildPersisted("testPublishSurvey");
        AdminUser user = adminUserFactory.buildPersisted("testPublishSurvey");
        String oldContent = survey.getContent();
        String newContent = String.format("{\"pages\":[],\"title\":\"%s\"}", RandomStringUtils.randomAlphabetic(6));
        survey.setContent(newContent);
        Survey newSurvey = surveyService.createNewVersion(user, survey.getPortalId(), survey);

        Assertions.assertNotEquals(newSurvey.getId(), survey.getId());
        // check version was incremented and content was modified
        Assertions.assertEquals(survey.getVersion() + 1, newSurvey.getVersion());
        Assertions.assertEquals(newContent, newSurvey.getContent());

        // confirm the existing survey wasn't modified
        Survey fetchedOriginal = surveyService.find(survey.getId()).get();
        Assertions.assertEquals(oldContent, fetchedOriginal.getContent());
    }

    @Test
    public void testGetSurveyQuestionDefinitions() {
        Survey survey = surveyFactory.buildPersisted("testPublishSurvey");
        String surveyContent = """
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

        survey.setContent(surveyContent);

        List<SurveyQuestionDefinition> actual = surveyService.getSurveyQuestionDefinitions(survey);
        Assertions.assertEquals(4, actual.size());
    }

    @Test
    public void testUnmarshalQuestionChoices() throws JsonProcessingException {
        String questionWithChoices = """
                {
                  "name": "oh_oh_cardioHx_coronaryDiseaseProcedure",
                  "type": "radiogroup",
                  "title": "Have you had any of the following treatments?",
                  "choices": [
                    {
                      "text": "Cardiac stent placement",
                      "value": "cardiacStentPlacement"
                    },
                    {
                      "text": "Cardiac bypass surgery",
                      "value": "cardiacBypassSurgery"
                    },
                    {
                      "text": "None of these",
                      "value": "noneOfThese"
                    }
                  ]
                }""";

        ObjectMapper mapper = new ObjectMapper();
        JsonNode questionNode = mapper.readTree(questionWithChoices);

        String actual = SurveyUtils.unmarshalSurveyQuestionChoices(questionNode);
        String expected = """
                {"noneOfThese":"None of these","cardiacBypassSurgery":"Cardiac bypass surgery","cardiacStentPlacement":"Cardiac stent placement"}""";

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testGetChildElements() throws JsonProcessingException {
        String questionWithChildren = """
                {
                    "type": "panel",
                    "title": "Heart valve disease",
                    "visibleIf": "{oh_oh_cardioHx_diagnosedHeartConditions} contains 'heartValveDisease'",
                    "elements": [
                      {
                        "name": "oh_oh_cardioHx_otherHeartCondition",
                        "type": "text",
                        "title": "What is the other heart or blood condition?"
                      },
                      {
                        "name": "oh_oh_cardioHx_heartValveProcedure",
                        "type": "radiogroup",
                        "title": "Have you ever had any of the following:",
                        "choices": [
                          {
                            "text": "Open heart surgery for heart valve",
                            "value": "openHeartSurgeryValve"
                          },
                          {
                            "text": "Valve replacement by a catheter (TAVR, TAVI)",
                            "value": "valveReplacement"
                          },
                          {
                            "text": "Mitral valve repair by a catheter (Mitraclip)",
                            "value": "mitralValveRepair"
                          },
                          {
                            "text": "None of these",
                            "value": "none"
                          }
                        ]
                      }
                    ]
                  }""";

        ObjectMapper mapper = new ObjectMapper();
        JsonNode questionNode = mapper.readTree(questionWithChildren);

        List<JsonNode> actual = SurveyUtils.getAllQuestions(questionNode);

        Assertions.assertEquals(2, actual.size());
    }

    @Test
    public void testResolvingQuestionTemplate() throws JsonProcessingException {
        String questionWithChoices = """
                {
                  "name": "oh_oh_cardioHx_coronaryDiseaseProcedure",
                  "type": "radiogroup",
                  "title": "Have you had any of the following treatments?",
                  "choices": [
                    {
                      "text": "Cardiac stent placement",
                      "value": "cardiacStentPlacement"
                    },
                    {
                      "text": "Cardiac bypass surgery",
                      "value": "cardiacBypassSurgery"
                    },
                    {
                      "text": "None of these",
                      "value": "noneOfThese"
                    }
                  ]
                }""";

        ObjectMapper mapper = new ObjectMapper();
        JsonNode questionNode = mapper.readTree(questionWithChoices);

        String actual = SurveyUtils.unmarshalSurveyQuestionChoices(questionNode);
        String expected = """
                {"noneOfThese":"None of these","cardiacBypassSurgery":"Cardiac bypass surgery","cardiacStentPlacement":"Cardiac stent placement"}""";

        Assertions.assertEquals(expected, actual);
    }

}
