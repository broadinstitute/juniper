package bio.terra.pearl.core.util;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.survey.SurveyParseUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SurveyParseUtilsTests extends BaseSpringBootTest {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    SurveyFactory surveyFactory;

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

        String actual = SurveyParseUtils.unmarshalSurveyQuestionChoices(questionNode);
        String expected = """
                [{"stableId":"cardiacStentPlacement","text":"Cardiac stent placement"},{"stableId":"cardiacBypassSurgery","text":"Cardiac bypass surgery"},{"stableId":"noneOfThese","text":"None of these"}]""";

        assertEquals(expected, actual);
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

        List<JsonNode> actual = SurveyParseUtils.getAllQuestions(questionNode);

        assertEquals(2, actual.size());
    }

    @Test
    public void testGetDynamicPanelElements() throws JsonProcessingException {
        String dynamicPanel = """
                {
                    "name": "examplePanel",
                    "type": "paneldynamic",
                    "title": "First name",
                    "maxPanelCount": 2,
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
                """;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode questionNode = mapper.readTree(dynamicPanel);

        List<JsonNode> actual = SurveyParseUtils.getAllQuestions(questionNode);

        assertEquals(3, actual.size());

        JsonNode panel = actual.get(0);
        JsonNode firstName = actual.get(1);
        JsonNode lastName = actual.get(2);


        assertEquals("examplePanel", panel.get("name").asText());
        assertEquals("firstName", firstName.get("name").asText());
        assertEquals("lastName", lastName.get("name").asText());
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

        String actual = SurveyParseUtils.unmarshalSurveyQuestionChoices(questionNode);
        String expected = """
                [{"stableId":"cardiacStentPlacement","text":"Cardiac stent placement"},{"stableId":"cardiacBypassSurgery","text":"Cardiac bypass surgery"},{"stableId":"noneOfThese","text":"None of these"}]""";

        assertEquals(expected, actual);
    }

    @Test
    public void testParseQuestionChoicesTranslated(TestInfo info) throws JsonProcessingException {
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
                      "text": {"en": "Cardiac bypass surgery", "es": "Cirugía de bypass cardíaco"},
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

        String actual = SurveyParseUtils.unmarshalSurveyQuestionChoices(questionNode);
        String expected = """
                [{"stableId":"cardiacStentPlacement","text":"Cardiac stent placement"},{"stableId":"cardiacBypassSurgery","text":"Cardiac bypass surgery"},{"stableId":"noneOfThese","text":"None of these"}]
                """;

        assertEquals(expected.strip(), actual.strip());
    }

    @Test
    public void testResolvingQuestionDropdown() throws JsonProcessingException {
        String questionWithChoices = """
                {
                  "name": "oh_oh_cardioHx_coronaryDiseaseProcedure",
                  "type": "dropdown",
                  "title": "Have you had any of the following treatments?",
                  "choices": [ "foo", "bar", "baz"]
                }""";

        ObjectMapper mapper = new ObjectMapper();
        JsonNode questionNode = mapper.readTree(questionWithChoices);

        String actual = SurveyParseUtils.unmarshalSurveyQuestionChoices(questionNode);
        String expected = """
                [{"stableId":"foo","text":"foo"},{"stableId":"bar","text":"bar"},{"stableId":"baz","text":"baz"}]""";

        assertEquals(expected, actual);
    }

    @Test
    public void testDetermineUpstreamStableId() throws Exception {
        String simpleDerivedQString = """
                {
                  "name": "someQuestion",
                  "expression": "df {upstreamQ} = 'foo'"
                }""";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode questionNode = mapper.readTree(simpleDerivedQString);
        String upstreamStableId = SurveyParseUtils.getUpstreamStableId(questionNode);
        assertThat(upstreamStableId, equalTo("upstreamQ"));
    }

    @Test
    public void testDetermineUpstreamStableIdTrim() throws Exception {
        String simpleDerivedQString = """
                {
                  "name": "someQuestion",
                  "expression": "{ upstreamQ } = 'foo'"
                }""";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode questionNode = mapper.readTree(simpleDerivedQString);
        String upstreamStableId = SurveyParseUtils.getUpstreamStableId(questionNode);
        assertThat(upstreamStableId, equalTo("upstreamQ"));
    }

    @Test
    public void testDetermineUpstreamStableIdHandlesMultiple() throws Exception {
        String simpleDerivedQString = """
                {
                  "name": "someQuestion",
                  "expression": "{ upstreamQ } = 'foo' && {otherQ} = 4"
                }""";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode questionNode = mapper.readTree(simpleDerivedQString);
        String upstreamStableId = SurveyParseUtils.getUpstreamStableId(questionNode);
        assertThat(upstreamStableId, equalTo("otherQ"));
    }

    @Test
    public void getCalculatedValues() throws Exception {
        String form = """
                {"calculatedValues": [
                       {
                         "name": "qualified",
                         "expression": "{hd_hd_preenroll_southAsianAncestry} = 'yes'",
                         "includeIntoResult": true
                       },
                       {
                         "name": "transientThing",
                         "expression": "1 + 2",
                         "includeIntoResult": false
                       },
                       {
                         "name": "transientThing2",
                         "expression": "1 + 2"
                       }
                     ]}""";
        JsonNode surveyDef = objectMapper.readTree(form);
        List<JsonNode> calculatedValues = SurveyParseUtils.getCalculatedValues(surveyDef);
        assertThat(calculatedValues, hasSize(1));
        assertThat(calculatedValues.get(0).get("name").asText(), equalTo("qualified"));
    }

    @Test
    public void testDetermineUpstreamStableIdHandlesNone() throws Exception {
        String simpleDerivedQString = """
                {
                  "name": "someQuestion",
                  "expression": "blah blah blah"
                }""";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode questionNode = mapper.readTree(simpleDerivedQString);
        String upstreamStableId = SurveyParseUtils.getUpstreamStableId(questionNode);
        assertThat(upstreamStableId, equalTo(null));
    }

    @Test
    public void testParseNullTitle() {
        String nullTitleForm = """
                {
                  "showQuestionNumbers": "off",
                  "showProgressBar": "bottom",
                  "pages": []
                }""";

        Map<String, String> parsedTitles = SurveyParseUtils.parseSurveyTitle(nullTitleForm, "FallbackName");
        assertThat(parsedTitles, equalTo(Map.of("en", "FallbackName")));
    }

    @Test
    public void testParseTextTitle() {
        String textTitleForm = """
                {
                  "title": "The Basics",
                  "showQuestionNumbers": "off",
                  "showProgressBar": "bottom",
                  "pages": []
                }""";

        Map<String, String> parsedTitles = SurveyParseUtils.parseSurveyTitle(textTitleForm, "FallBackName");
        assertThat(parsedTitles, equalTo(Map.of("en", "The Basics")));
    }

    @Test
    public void testParseObjectTitle() {
        String objectTitleForm = """
                {
                  "title": {
                    "default": "The Basics",
                    "es": "Los Basicos",
                    "dev": "DEV_The Basics"
                  },
                  "showQuestionNumbers": "off",
                  "showProgressBar": "bottom",
                  "pages": []
                }""";

        Map<String, String> parsedTitles = SurveyParseUtils.parseSurveyTitle(objectTitleForm, "FallBackName");
        assertThat(parsedTitles, equalTo(Map.of("en", "The Basics", "es", "Los Basicos", "dev", "DEV_The Basics")));
    }

    @Test
    public void testParseTitlesUnparseableForm() {
        String unparseableForm = """
                {
                  "title": {
                    "default": "The Basics",
                    "es"[]]]]]]
                """;

        Map<String, String> parsedTitles = SurveyParseUtils.parseSurveyTitle(unparseableForm, "FallbackName");
        assertThat(parsedTitles, equalTo(Map.of("en", "FallbackName")));
    }

    @Test
    public void testParseCheckboxMultiOtherQuestions() throws JsonProcessingException {
        String form = """
                {
                  "elements": [
                    {
                      "name": "schools",
                      "type": "checkbox",
                      "title": "What schools did you attend?",
                      "renderAs": "checkbox-multiple-other",
                      "choices": [
                        {
                          "text": "MIT",
                          "value": "mit",
                          "otherStableId": "schoolsMitDetail",
                          "otherText": {
                              "en": "What did you study at MIT?",
                              "es": "¿Qué estudiaste en MIT?"
                          },
                          "otherPlaceHolder": {
                              "en": "Your major",
                              "es": "Tu especialidad"
                          }
                        },
                        {
                            "text": "Harvard",
                            "value": "harvard",
                            "otherStableId": "schoolsHarvardDetail",
                            "otherText": {
                              "en": "What did you study at Harvard?",
                              "es": "¿Qué estudiaste en Harvard?"
                            },
                            "otherPlaceHolder": {
                              "en": "Your major",
                              "es": "Tu especialidad"
                            }
                        },
                        {
                          "text": "Northeastern",
                          "value": "northeastern",
                          "otherStableId": "schoolsNortheasternDetail",
                          "otherText": {
                              "en": "What did you study at Northeastern?",
                              "es": "¿Qué estudiaste en Northeastern?"
                          },
                          "otherPlaceHolder": {
                              "en": "Your major",
                              "es": "Tu especialidad"
                          }
                        },
                        {
                          "text": "Boston University",
                          "value": "bu",
                          "otherStableId": "schoolsBuDetail",
                          "otherText": {
                              "en": "What did you study at Boston University?",
                              "es": "¿Qué estudiaste en Boston University?"
                          },
                          "otherPlaceHolder": {
                              "en": "Your major",
                              "es": "Tu especialidad"
                          }
                        },
                        {
                          "text": "Other",
                          "value": "other",
                          "otherStableId": "schoolsOtherDetail"
                        }
                      ]
                    }
                  ]
                }
                """;

        List<JsonNode> questions = SurveyParseUtils.getAllQuestions(objectMapper.readTree(form));

        assertThat(questions, hasSize(6));

        Set<String> stableIds = questions.stream().map(q -> q.get("name").asText()).collect(Collectors.toSet());
        assertEquals(Set.of(
                        "schools",
                        "schoolsMitDetail",
                        "schoolsHarvardDetail",
                        "schoolsNortheasternDetail",
                        "schoolsBuDetail",
                        "schoolsOtherDetail"),
                stableIds);
    }

    @Test
    public void testQuestionReferenceRoundTripsSameStudy() {
        SurveyParseUtils.QuestionReference reference = SurveyParseUtils.QuestionReference.fromString("survey1.diagnosis");
        assertEquals("survey1", reference.surveyStableId());
        assertEquals("diagnosis", reference.questionStableId());
        assertEquals(null, reference.studyShortcode());
        assertEquals("survey1.diagnosis", reference.toString());
    }

    @Test
    public void testQuestionReferenceRoundTripsCrossStudy() {
        SurveyParseUtils.QuestionReference reference = SurveyParseUtils.QuestionReference.fromString("survey1['otherStudy'].diagnosis");
        assertEquals("survey1", reference.surveyStableId());
        assertEquals("diagnosis", reference.questionStableId());
        assertEquals("otherStudy", reference.studyShortcode());
        assertEquals("survey1['otherStudy'].diagnosis", reference.toString());
    }

    @Test
    public void testParseReferencedSurveyQuestionsHandlesCrossStudy() throws JsonProcessingException {
        String form = """
                {
                  "pages": [
                    {
                      "elements": [
                        {
                          "name": "q1",
                          "type": "text",
                          "title": "Value is {survey1['otherStudy'].diagnosis}"
                        }
                      ]
                    }
                  ]
                }
                """;
        Survey survey = Survey.builder().content(form).build();

        List<SurveyParseUtils.QuestionReference> references = SurveyParseUtils.parseReferencedSurveyQuestions(survey);

        assertThat(references, hasSize(1));
        assertEquals("survey1", references.get(0).surveyStableId());
        assertEquals("otherStudy", references.get(0).studyShortcode());
        assertEquals("diagnosis", references.get(0).questionStableId());
    }

}
