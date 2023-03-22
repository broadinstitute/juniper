package bio.terra.pearl.core.util;

import bio.terra.pearl.core.BaseSpringBootTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SurveyUtilsTests extends BaseSpringBootTest {

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
