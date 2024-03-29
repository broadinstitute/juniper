package bio.terra.pearl.core.util;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.service.survey.SurveyParseUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class SurveyParseUtilsTests extends BaseSpringBootTest {

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

        List<JsonNode> actual = SurveyParseUtils.getAllQuestions(questionNode);

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

        String actual = SurveyParseUtils.unmarshalSurveyQuestionChoices(questionNode);
        String expected = """
                [{"stableId":"cardiacStentPlacement","text":"Cardiac stent placement"},{"stableId":"cardiacBypassSurgery","text":"Cardiac bypass surgery"},{"stableId":"noneOfThese","text":"None of these"}]""";

        Assertions.assertEquals(expected, actual);
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

        Assertions.assertEquals(expected, actual);
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
}
